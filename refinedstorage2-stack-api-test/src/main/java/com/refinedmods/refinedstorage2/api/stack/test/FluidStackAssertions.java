package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FluidStackAssertions {
    private FluidStackAssertions() {
    }

    public static void assertFluidStackListContents(StackList<Rs2FluidStack> actual, Rs2FluidStack... expected) {
       // assertFluidStackListContents(actual.getAll(), expected);
    }

    public static void assertFluidStackListContents(Collection<Rs2FluidStack> actual, Rs2FluidStack... expected) {
        Rs2FluidStackWrapper[] wrappers = new Rs2FluidStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new Rs2FluidStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(Rs2FluidStackWrapper::new).collect(Collectors.toList())).containsExactlyInAnyOrder(wrappers);
    }

    public static void assertOrderedFluidStackListContents(Collection<Rs2FluidStack> actual, Rs2FluidStack... expected) {
        Rs2FluidStackWrapper[] wrappers = new Rs2FluidStackWrapper[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            wrappers[i] = new Rs2FluidStackWrapper(expected[i]);
        }

        assertThat(actual.stream().map(Rs2FluidStackWrapper::new).collect(Collectors.toList())).containsExactly(wrappers);
    }

    public static void assertFluidStack(Rs2FluidStack actual, Rs2FluidStack expected) {
        assertThat(new Rs2FluidStackWrapper(actual)).isEqualTo(new Rs2FluidStackWrapper(expected));
    }

    private record Rs2FluidStackWrapper(Rs2FluidStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Rs2FluidStackWrapper that = (Rs2FluidStackWrapper) o;
            return Objects.equals(stack.getFluid(), that.stack.getFluid())
                    && Objects.equals(stack.getTag(), that.stack.getTag())
                    && stack.getAmount() == that.stack.getAmount();
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getFluid(), stack.getTag(), stack.getAmount());
        }

        @Override
        public String toString() {
            return stack.toString();
        }
    }
}
