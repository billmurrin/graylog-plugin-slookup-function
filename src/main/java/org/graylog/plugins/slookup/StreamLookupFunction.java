package org.graylog.plugins.slookup;

import org.graylog2.indexer.results.ScrollResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.plugin.indexer.searches.timeranges.TimeRange;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class StreamLookupFunction extends AbstractFunction<String> {
    Logger LOG = LoggerFactory.getLogger(Function.class);

    public static final String NAME = "slookup";
    private static final String STREAM_ARG = "string";
    private static final String SRC_FIELD_ARG = "string";
    private static final String DST_FIELD_ARG = "string";
    private static final String RTN_FIELD_ARG = "string";
    private static final String TIMERANGE_ARG = "integer";

    private String query;
    private String filter;
    private Searches searches;
    private TimeRange timeRange;
    private Sorting sortType;
    private List<String> fields = new ArrayList<>();

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

        String stream = streamParam.required(functionArgs, evaluationContext);
        String srcField = srcFieldParam.required(functionArgs, evaluationContext);
        String dstField = dstFieldParam.required(functionArgs, evaluationContext);
        String rtnField = rtnFieldParam.required(functionArgs, evaluationContext);
        Integer timeRange = Integer.parseInt(timeRangeParam.required(functionArgs, evaluationContext));

        this.fields.add(rtnField);
        ScrollResult search;

        //Currently defaulting to 12 hours
        this.timeRange = RelativeRange.builder().range(timeRange).build();
        LOG.debug("The TimeRange is " + this.timeRange);

        // Trying to build a query string here.
        this.query = dstField + ":" + evaluationContext.currentMessage().getField(srcField).toString();
        LOG.debug("This query: " + this.query);

        this.filter = "streams:" + stream;
        LOG.debug("This filter: " + this.filter);

        // Attempt to do the search here.
        //ScrollResult scroll(String query, TimeRange range, int limit, int offset, List<String> fields, String filter)
        search = this.searches.scroll(this.query, this.timeRange, 1, 0, fields, this.filter);
        //search(java.lang.String query, java.lang.String filter, org.graylog2.plugin.indexer.searches.timeranges.TimeRange range, int limit, int offset, org.graylog2.indexer.searches.Sorting sorting)
        //SearchResult search = this.searches.search(this.query, this.filter, this.timeRange, 1, 0, new Sorting("timestamp", Sorting.Direction.DESC));

        // Echo the results
        LOG.debug(search.toString());

        //if (target == null) {
        //    return 0;
        //}

        //return target.length();
        return search.toString();
    }

    @Override
    public FunctionDescriptor<String> descriptor() {
        return FunctionDescriptor.<String>builder()
                .name(NAME)
                .description("Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel")
                .params(streamParam)
                .params(srcFieldParam)
                .params(dstFieldParam)
                .params(rtnFieldParam)
                .params(timeRangeParam)
                .returnType(String.class)
                .build();
    }
}
