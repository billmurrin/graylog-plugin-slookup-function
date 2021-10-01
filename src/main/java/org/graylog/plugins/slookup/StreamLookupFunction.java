package org.graylog.plugins.slookup;

import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.search.sort.SortOrder;

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

public class StreamLookupFunction extends AbstractFunction<List> {
    Logger LOG = LoggerFactory.getLogger(Function.class);

    public static final String NAME = "slookup";
    private static final String STREAM_ARG = "stream";
    private static final String SRC_FIELD_ARG = "srcField";
    private static final String DST_FIELD_ARG = "dstField";
    private static final String RTN_FIELDS_ARG = "rtnFields";
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
    private final ParameterDescriptor<List, List> rtnFieldsParam = ParameterDescriptor
            .type(RTN_FIELDS_ARG, List.class)
            .description("The field(s) to return if there is a value match.")
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
    public List<String> evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        String stream = streamParam.required(functionArgs, evaluationContext);
        String srcField = srcFieldParam.required(functionArgs, evaluationContext);
        String dstField = dstFieldParam.required(functionArgs, evaluationContext);
        List<String> rtnFields = (List<String>) rtnFieldsParam.required(functionArgs, evaluationContext);
        Integer timeRange = Integer.parseInt(timeRangeParam.required(functionArgs, evaluationContext));
        String sortField = sortOrderParam.required(functionArgs, evaluationContext);
        List<String> resultList = new ArrayList<String>();
        List<String> blankList = new ArrayList<String>();

        for (String rtnfl : rtnFields) {
            blankList.add("No match found");
        }

        //this.timeRange = RelativeRange.builder().type("relative").range(timeRange).build();
	//this was changed in Graylog 4.1 to include an optional from/to instead of just range
        try {
            this.timeRange = RelativeRange.create(timeRange.intValue());
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }	 

        String srcFieldValue = evaluationContext.currentMessage().getField(srcField).toString();
        String escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";

        if (!dstField.equals("timestamp")) {
            this.query = dstField + ":" + srcFieldValue.replaceAll(escapeChars, "\\\\$0");
            LOG.debug("SLookup Query: {}", this.query.toString());
        }

        this.filter = "streams:" + stream;
        LOG.debug("Filter: {}", this.filter.toString());

        if (sortField.equals("asc")) {
            this.sortType = new Sorting("timestamp", Sorting.Direction.ASC);
            LOG.debug("This sortType  - field: {}, order: {}", this.sortType.getField().toString(), this.sortType.toString());
        }
        else
        {
            this.sortType = new Sorting("timestamp", Sorting.Direction.DESC);
            LOG.debug("This sortType  - field: {}, order: {}", this.sortType.getField().toString(), this.sortType.toString());
        }

        final SearchesConfig searchesConfig = SearchesConfig.builder()
                .query(this.query)
                .filter(this.filter)
                .fields(rtnFields)
                .range(this.timeRange)
                .sorting(this.sortType)
                .limit(1)
                .offset(0)
                .build();

        try {
            SearchResult response = this.searches.search(searchesConfig);
            LOG.debug("Search config - field: {}, order: {}", searchesConfig.sorting().getField().toString(), searchesConfig.sorting().toString());
            if (response.getResults().size() == 0) {
                LOG.debug("No Search Results observed.");
                return blankList;
            }
            else
            {

                if (response.getResults().size() >= 1) {
                    LOG.debug("There are results");
                    List<ResultMessage> resultMessages = response.getResults();
                    try {
                        //Map<String, Object> resultFields = resultMessages.get(0).getMessage().getFields();
                        Message msg = resultMessages.get(0).getMessage();
                        if (msg.getFields().size() > 0) {
                            for(String rtnField : rtnFields) {
                                try {
                                    LOG.debug("Current return field: {}", rtnField);
                                    String returnStrField = String.valueOf(msg.getField(rtnField));
                                    if (!returnStrField.isEmpty()) {
                                        LOG.debug("Field: {}, Value: {}", rtnField, returnStrField);
                                        resultList.add(returnStrField);
                                        LOG.debug("Added value to resultList");
                                    } else {
                                        LOG.debug("Return field is empty");
                                    }
                                } catch(Exception e) {
                                    LOG.debug("Unable to retrieve field value: {} for field {}", e.getMessage(), rtnField);
                                    resultList.add("No match found");
                                }
                            }
                        }

                        if (resultList.isEmpty()) {
                            LOG.debug("Return List is empty. Exiting");
                            return blankList;
                        }
                        else
                        {
                            LOG.debug("Return List not empty. The return field(s) are {}, the values is {}", rtnFields.toString(), resultList.toString());
                            return resultList;
                        }

                    } catch(Exception e) {
                        LOG.debug("Error retrieving message: {} {}", e.getMessage(), e.toString());
                        return blankList;
                    }
                } else {
                    LOG.debug("No results");
                    return blankList;
                }
            }
        } catch(SearchPhaseExecutionException e) {
            LOG.debug("Unable to execute search: {}", e.getMessage());
            return blankList;
        }
    }

    @Override
    public FunctionDescriptor<List> descriptor() {
        return FunctionDescriptor.<List>builder()
                .name(NAME)
                .description("Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel")
                .params(of(streamParam, srcFieldParam, dstFieldParam, rtnFieldsParam, timeRangeParam, sortOrderParam))
                .returnType(List.class)
                .build();
    }
}
