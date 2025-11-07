package net.laurus.data;

import java.util.function.Supplier;

import net.laurus.builder.LayeredShapeBuilder;
import net.laurus.builder.PsuPcbEnclosureBuilder;
import net.laurus.builder.SplitFanPlateBuilder;
import net.laurus.builder.SuperMicroFanPlateBuilder;

public enum ShapeType {

    SUPER_MICRO_FAN(
            "Super Micro Fan Plate",
            () ->
            {
                SuperMicroFanPlateBuilder fanPlate = SuperMicroFanPlateBuilder
                        .builder()
                        .addCableRouting(true)
                        .build();
                return SplitFanPlateBuilder.builder().source(fanPlate).build().build();
            }
    ),
    HP_PSU_HOUSING(
            "HP Common Slot PSU Housing",
            () ->
            {
                return PsuPcbEnclosureBuilder.builder().build().build();
            }
    );

    private final String name;

    private final Supplier<LayeredShapeBuilder> builderSupplier;

    ShapeType(String name, Supplier<LayeredShapeBuilder> builderSupplier) {
        this.name = name;
        this.builderSupplier = builderSupplier;
    }

    public LayeredShapeBuilder createBuilder() {
        return builderSupplier.get();
    }

    @Override
    public String toString() {
        return name;
    }

}
