package org.graylog.plugins.slookup;

//import org.graylog2.indexer.results.ScrollResult;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.searches.SearchesConfig;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.indexer.ranges.IndexRangeService;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.*;
import static com.google.common.collect.ImmutableList.of;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graylog2.Configuration;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.List;

public class StreamLookupFunction extends AbstractFunction<String> {
    Logger LOG = LoggerFactory.getLogger(Function.class);

    public static final String NAME = "slookup";
    private static final String STREAM_ARG = "stream";
    private static final String SRC_FIELD_ARG = "srcField";
    private static final String DST_FIELD_ARG = "dstField";
    private static final String RTN_FIELD_ARG = "rtnField";
    private static final String TIMERANGE_ARG = "timeRange";

    private String query;
    private String filter;
    private TimeRange timeRange;
    private Sorting sortType;
    private Searches searches;
    private SearchResult response;
    //private final Configuration configuration = new;
    //private final IndexRangeService indexRangeService;
    //private final Client c;

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

    @Override
    public Object preComputeConstantArgument(FunctionArgs functionArgs, String s, Expression expression) {
        return expression.evaluateUnsafe(EvaluationContext.emptyContext());
    }

    @Override
    public String evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        LOG.info(evaluationContext.toString());
        //this.searches = new Searches(this.configuration, )
        String stream = streamParam.required(functionArgs, evaluationContext);
        String srcField = srcFieldParam.required(functionArgs, evaluationContext);
        String dstField = dstFieldParam.required(functionArgs, evaluationContext);
        String rtnField = rtnFieldParam.required(functionArgs, evaluationContext);
        Integer timeRange = Integer.parseInt(timeRangeParam.required(functionArgs, evaluationContext));
        boolean decorate = true;
        List<String> fields = new ArrayList<>();


        fields.add(rtnField);
        //ScrollResult search;


        LOG.info("Stream is " + stream);
        LOG.info("srcField is " + srcField);
        LOG.info("dstField is " + dstField);
        LOG.info("rtnField is " + rtnField);
        LOG.info("timeRange is " + timeRange.toString());

        //Currently defaulting to 12 hours
        this.timeRange = RelativeRange.builder().type("relative").range(timeRange).build();
        LOG.info("The TimeRange is " + this.timeRange);

        // Trying to build a query string here.
        this.query = dstField + ":" + evaluationContext.currentMessage().getField(srcField).toString();
        LOG.info("This query: " + this.query);

        this.filter = "streams:" + stream;
        LOG.info("This filter: " + this.filter);

        LOG.info("This fields" + fields.toString());

        final SearchesConfig searchesConfig = SearchesConfig.builder()
                .query(this.query)
                .filter(this.filter)
                .fields(fields)
                .range(this.timeRange)
                .limit(1)
                .offset(0)
                .build();

        LOG.info("Config is " + searchesConfig.toString());

        // Attempt to do the search here.
        //ScrollResult scroll(String query, TimeRange range, int limit, int offset, List<String> fields, String filter)
        //search = this.searches.scroll(this.query, this.timeRange, 1, 0, fields, this.filter);
        //search(java.lang.String query, java.lang.String filter, org.graylog2.plugin.indexer.searches.timeranges.TimeRange range, int limit, int offset, org.graylog2.indexer.searches.Sorting sorting)
        try {
            LOG.info("Trying the search");
            response = this.searches.search(searchesConfig);
            LOG.info("Response results" + response.getResults().toString());
        } catch(SearchPhaseExecutionException e) {
            LOG.info("Unable to execute search: {}", e.getMessage());
        }

        //if (target == null) {
        //    return 0;
        //}

        //return target.length();
        return response.getResults().toString();
    }

    @Override
    public FunctionDescriptor<String> descriptor() {
        return FunctionDescriptor.<String>builder()
                .name(NAME)
                .description("Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel")
                .params(of(streamParam, srcFieldParam, dstFieldParam, rtnFieldParam, timeRangeParam))
                .returnType(String.class)
                .build();
    }


}
