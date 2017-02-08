package org.graylog.plugins.slookup;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

public class StreamLookupFunction {

    public static final String NAME = "slookup";
    private static final String PARAM = "string";
    private static final String PARAM2 = "string";
    private static final String PARAM3 = "string";

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .string(PARAM2)
            .string(PARAM3)
            .description("The field to return the value of. For example, passing 'foo' will return 3.")
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
