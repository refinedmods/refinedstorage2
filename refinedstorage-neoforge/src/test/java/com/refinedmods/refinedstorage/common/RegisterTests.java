package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterTest;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorTest;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorTest;
import com.refinedmods.refinedstorage.common.controller.ControllerTest;
import com.refinedmods.refinedstorage.common.detector.DetectorTest;
import com.refinedmods.refinedstorage.common.exporter.ExporterTest;
import com.refinedmods.refinedstorage.common.iface.InterfaceTest;
import com.refinedmods.refinedstorage.common.importer.ImporterTest;
import com.refinedmods.refinedstorage.common.networking.CableLikePartsConnectionTest;
import com.refinedmods.refinedstorage.common.networking.FullBlocksConnectionTest;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterReceiverTest;
import com.refinedmods.refinedstorage.common.networking.RelayTest;
import com.refinedmods.refinedstorage.common.storage.diskdrive.FluidDiskDriveTest;
import com.refinedmods.refinedstorage.common.storage.diskdrive.ItemDiskDriveTest;
import com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceTest;
import com.refinedmods.refinedstorage.common.storage.externalstorage.FluidExternalStorageTest;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ItemExternalStorageTest;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlockTest;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlockTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.CaseFormat;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

@EventBusSubscriber(modid = MOD_ID)
public class RegisterTests {
    private static final boolean GENERATE_INSTANCE_JSONS = true;
    private static final List<Class<?>> TEST_CLASSES = List.of(
        AutocrafterTest.class,
        ConstructorTest.class,
        DestructorTest.class,
        ControllerTest.class,
        DetectorTest.class,
        ExporterTest.class,
        InterfaceTest.class,
        ImporterTest.class,
        NetworkTransmitterReceiverTest.class,
        CableLikePartsConnectionTest.class,
        FullBlocksConnectionTest.class,
        RelayTest.class,
        FluidDiskDriveTest.class,
        ItemDiskDriveTest.class,
        DiskInterfaceTest.class,
        FluidExternalStorageTest.class,
        ItemExternalStorageTest.class,
        FluidStorageBlockTest.class,
        ItemStorageBlockTest.class
    );

    private RegisterTests() {
    }

    @SubscribeEvent
    public static void registerTests(final RegisterEvent e) {
        final List<TestFunction> testFunctions = getTestFunctions();
        if (GENERATE_INSTANCE_JSONS) {
            testFunctions.forEach(RegisterTests::generateInstanceJson);
        }
        e.register(Registries.TEST_FUNCTION, helper ->
            testFunctions.forEach(testFunction ->
                helper.register(createIdentifier(testFunction.name), testFunction.handle)));
    }

    private static void generateInstanceJson(final TestFunction testFunction) {
        final String path =
            "../src/test/resources/data/" + MOD_ID + "/test_instance/%s.json".formatted(testFunction.name);
        try {
            Files.writeString(Path.of(path), """
                {
                  "type": "minecraft:function",
                  "function": "%s:%s",
                  "structure": "%s:empty_15x15",
                  "environment": "minecraft:default",
                  "max_ticks": 400
                }
                """.formatted(MOD_ID, testFunction.name, MOD_ID));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<TestFunction> getTestFunctions() {
        final List<TestFunction> testFunctions = new ArrayList<>();
        final Set<String> testFunctionNames = new HashSet<>();
        for (final Class<?> testClass : TEST_CLASSES) {
            try {
                for (final Method method : testClass.getMethods()) {
                    if (method.getAnnotation(MinecraftIntegrationTest.class) != null) {
                        final TestFunction testFunction = TestFunction.of(testClass, method);
                        if (!testFunctionNames.add(testFunction.name)) {
                            throw new IllegalStateException("Duplicate test function name: " + testFunction.name);
                        }
                        testFunctions.add(testFunction);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return testFunctions;
    }

    private record TestFunction(String name, Consumer<GameTestHelper> handle) {
        public static TestFunction of(final Class<?> clazz, final Method method) {
            final String name =
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName().replace("Test", ""))
                    + "_"
                    + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getName());
            return new TestFunction(name, helper -> {
                try {
                    method.invoke(null, helper);
                } catch (final IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
}
