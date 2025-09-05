package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.TestResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class InitialStateExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return MutableResourceList.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
        throws ParameterResolutionException {
        final Optional<InitialState> annotation = parameterContext.findAnnotation(InitialState.class);
        final TestResource[] initialState = annotation
            .map(InitialState::value)
            .orElse(new TestResource[0]);
        final long amount = annotation.map(InitialState::amount).orElse(0L);
        final Class<?> declaringClass = parameterContext.getDeclaringExecutable().getDeclaringClass();
        try {
            final Method createList = declaringClass.getDeclaredMethod("createList", TestResource[].class, long.class);
            return createList.invoke(extensionContext.getTestInstance().orElseThrow(), initialState, amount);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ParameterResolutionException("createList method could not be invoked", e);
        }
    }
}
