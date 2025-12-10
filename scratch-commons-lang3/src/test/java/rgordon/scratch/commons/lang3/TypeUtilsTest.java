package rgordon.scratch.commons.lang3;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TypeUtilsTest {

    public static class ThingWithFunctions {

        public Function<String, Integer> stringToInt() {
            return Integer::parseInt;
        }

        public void needsStringToInt(Function<? super String, ? extends Integer> func) {
        }
    }


    @Test
    void convertAssumptions() throws NoSuchMethodException {

        final Type retType = ThingWithFunctions.class.getDeclaredMethod("stringToInt")
                .getGenericReturnType();

        final Type paraType = ThingWithFunctions.class.getDeclaredMethod("needsStringToInt", Function.class)
                .getGenericParameterTypes()[0];

        assertThat(TypeUtils.isAssignable(retType, paraType), is(true));
    }

}
