package com.example.plugins.strlen;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

public class StringLengthFunction implements Function<Integer> {

    public static final String NAME = "string_length";
    private static final String PARAM = "string";

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .description("The string to calculate the length of. For example, passing 'foo' will return 3.")
            .build();

    @Override
    public Object preComputeConstantArgument(FunctionArgs functionArgs, String s, Expression expression) {
        return expression.evaluateUnsafe(EvaluationContext.emptyContext());
    }

    @Override
    public Integer evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        String target = valueParam.required(functionArgs, evaluationContext);

        if (target == null) {
            return 0;
        }

        return target.length();
    }

    @Override
    public FunctionDescriptor<Integer> descriptor() {
        return FunctionDescriptor.<Integer>builder()
                .name(NAME)
                .description("Returns the length of a string")
                .params(valueParam)
                .returnType(Integer.class)
                .build();
    }

}
