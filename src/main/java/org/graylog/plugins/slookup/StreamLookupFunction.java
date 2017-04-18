package org.graylog.plugins.slookup;

import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.graylog2.indexer.results.ResultMessage;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.searches.SearchesConfig;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.*;
import static com.google.common.collect.ImmutableList.of;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class StreamLookupFunction extends AbstractFunction<String> {
    Logger LOG = LoggerFactory.getLogger(Function.class);

    public static final String NAME = "slookup";
    private static final String STREAM_ARG = "stream";
    private static final String SRC_FIELD_ARG = "srcField";
    private static final String DST_FIELD_ARG = "dstField";
    private static final String RTN_FIELD_ARG = "rtnField";
    private static final String TIMERANGE_ARG = "timeRange";
    private static final String SORTORDER_ARG = "sortOrder";

    private String query;
    private String filter;
    private TimeRange timeRange;
    private Sorting sortType;
    private SearchResult response;
    private final Searches searches;

    @Inject
    public StreamLookupFunction(Searches searches) {
        this.searches = searches;
    }

    private final ParameterDescriptor<String, String> streamParam = ParameterDescriptor
            .string(STREAM_ARG)
            .description("The stream to look up the source field.")
            .build();
    private final ParameterDescriptor<String, String> srcFieldParam = ParameterDescriptor
            .string(SRC_FIELD_ARG)
            .description("The source field to lookup and match in the stream.")
            .build();
    private final ParameterDescriptor<String, String> dstFieldParam = ParameterDescriptor
            .string(DST_FIELD_ARG)
            .description("The destination field that will be matched against with the source field. If blank, uses the value of the source field.")
            .build();
    private final ParameterDescriptor<String, String> rtnFieldParam = ParameterDescriptor
            .string(RTN_FIELD_ARG)
            .description("The field to return if there is a value match.")
            .build();
    private final ParameterDescriptor<String, String> timeRangeParam = ParameterDescriptor
            .string(TIMERANGE_ARG)
            .description("Relative Time Range")
            .build();
    private final ParameterDescriptor<String, String> sortOrderParam = ParameterDescriptor
            .string(SORTORDER_ARG)
            .description("Sorting Order - asc or desc")
            .build();

    @Override
    public Object preComputeConstantArgument(FunctionArgs functionArgs, String s, Expression expression) {
        return expression.evaluateUnsafe(EvaluationContext.emptyContext());
    }

    @Override
    public String evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        String stream = streamParam.required(functionArgs, evaluationContext);
        String srcField = srcFieldParam.required(functionArgs, evaluationContext);
        String dstField = dstFieldParam.required(functionArgs, evaluationContext);
        String rtnField = rtnFieldParam.required(functionArgs, evaluationContext);
        Integer timeRange = Integer.parseInt(timeRangeParam.required(functionArgs, evaluationContext));
        String sortField = sortOrderParam.required(functionArgs, evaluationContext);

        List<String> fields = new ArrayList<>();

        fields.add(rtnField);

        this.timeRange = RelativeRange.builder().type("relative").range(timeRange).build();

        this.query = dstField + ":" + evaluationContext.currentMessage().getField(srcField).toString();

        this.filter = "streams:" + stream;

        if (sortField.equals("asc")) {
            this.sortType = new Sorting("timestamp", Sorting.Direction.ASC);
            LOG.debug("This sortType  - field: {}, order: {}", this.sortType.getField().toString(), this.sortType.asElastic().toString());
        }
        else
        {
            this.sortType = new Sorting("timestamp", Sorting.Direction.DESC);
            LOG.debug("This sortType  - field: {}, order: {}", this.sortType.getField().toString(), this.sortType.asElastic().toString());
        }

        final SearchesConfig searchesConfig = SearchesConfig.builder()
                .query(this.query)
                .filter(this.filter)
                .fields(fields)
                .range(this.timeRange)
                .sorting(this.sortType)
                .limit(1)
                .offset(0)
                .build();

        try {
            SearchResult response = this.searches.search(searchesConfig);
            LOG.debug("Search config - field: {}, order: {}", searchesConfig.sorting().getField().toString(), searchesConfig.sorting().asElastic().toString());
            if (response.getResults().size() == 0) {
                LOG.info("No Search Results observed.");
                return "";
            }
            else
            {
                List<ResultMessage> resultMessages = response.getResults();
                Message msg = resultMessages.get(0).getMessage();
                String returnField = msg.getField(rtnField).toString();
                LOG.debug("The return field is {}, the value is {}", rtnField, returnField);
                if (returnField.isEmpty()) {
                    return "";
                }
                else
                {
                    return returnField;
                }
            }
        } catch(SearchPhaseExecutionException e) {
            LOG.debug("Unable to execute search: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public FunctionDescriptor<String> descriptor() {
        return FunctionDescriptor.<String>builder()
                .name(NAME)
                .description("Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel")
                .params(of(streamParam, srcFieldParam, dstFieldParam, rtnFieldParam, timeRangeParam, sortOrderParam))
                .returnType(String.class)
                .build();
    }


}
