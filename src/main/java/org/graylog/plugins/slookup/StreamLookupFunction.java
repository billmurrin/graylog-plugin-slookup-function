package org.graylog.plugins.slookup;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamLookupFunction extends AbstractFunction<String> {
    Logger log = LoggerFactory.getLogger(Function.class);

    public static final String NAME = "slookup";
    private static final String STREAM_ARG = "string";
    private static final String SRC_FIELD_ARG = "string";
    private static final String DST_FIELD_ARG = "string";
    private static final String RTN_FIELD_ARG = "string";

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

    @Override
    public Object preComputeConstantArgument(FunctionArgs functionArgs, String s, Expression expression) {
        return expression.evaluateUnsafe(EvaluationContext.emptyContext());
    }

    @Override
    public String evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        String target = streamParam.required(functionArgs, evaluationContext);

        //if (target == null) {
        //    return 0;
        //}

        //return target.length();
        return target;
    }

    @Override
    public FunctionDescriptor<String> descriptor() {
        return FunctionDescriptor.<String>builder()
                .name(NAME)
                .description("Returns the length of a string")
                .params(streamParam)
                .returnType(String.class)
                .build();
    }
}
