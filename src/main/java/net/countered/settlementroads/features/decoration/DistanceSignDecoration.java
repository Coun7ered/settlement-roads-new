package net.countered.settlementroads.features.decoration;

import net.countered.settlementroads.config.ModConfig;
import net.countered.settlementroads.features.decoration.util.BiomeWoodAware;
import net.countered.settlementroads.helpers.Records;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;

import java.util.Objects;

public class DistanceSignDecoration extends OrientedDecoration implements BiomeWoodAware {
    private final boolean isStart;
    private final int distance;
    private Records.WoodAssets wood;

    public DistanceSignDecoration(BlockPos pos, Vec3i direction, StructureWorldAccess world, Boolean isStart, int distance) {
        super(pos, direction, world);
        this.isStart = isStart;
        this.distance = distance;
    }

    @Override
    public void place() {
        if (!placeAllowed()) return;

        int rotation = getCardinalRotationFromVector(getOrthogonalVector(), isStart);
        DirectionProperties props = getDirectionProperties(rotation);

        BlockPos basePos = this.getPos();
        StructureWorldAccess world = this.getWorld();

        BlockPos signPos = basePos.up(2).offset(props.offsetDirection.getOpposite());
        world.setBlockState(signPos, wood.hangingSign().getDefaultState()
                .with(Properties.ROTATION, rotation)
                .with(Properties.ATTACHED, true), 3);
        updateSigns(world, signPos, distance);

        placeFenceStructure(basePos, props);
    }

    private void placeFenceStructure(BlockPos pos, DirectionProperties props) {
        StructureWorldAccess world = this.getWorld();

        world.setBlockState(pos.up(3).offset(props.offsetDirection.getOpposite()), wood.fence().getDefaultState().with(props.directionProperty, true), 3);
        world.setBlockState(pos.up(0), wood.fence().getDefaultState(), 3);
        world.setBlockState(pos.up(1), wood.fence().getDefaultState(), 3);
        world.setBlockState(pos.up(2), wood.fence().getDefaultState(), 3);
        world.setBlockState(pos.up(3), wood.fence().getDefaultState().with(props.reverseDirectionProperty, true), 3);
    }

    private void updateSigns(StructureWorldAccess structureWorldAccess, BlockPos surfacePos, int distance) {
        Objects.requireNonNull(structureWorldAccess.getServer()).execute( () -> {
            BlockEntity signEntity = structureWorldAccess.getBlockEntity(surfacePos);
            if (signEntity instanceof HangingSignBlockEntity signBlockEntity) {
                signBlockEntity.setWorld(structureWorldAccess.toServerWorld());
                addTextToHangingSign(signBlockEntity, ModConfig.nextVillageSignText, distance, true);
                addTextToHangingSign(signBlockEntity, ModConfig.helloSignText, distance, false);
                signBlockEntity.markDirty();
            }
        });
    }

    private static void addTextToHangingSign(HangingSignBlockEntity signBlockEntity, String text, int distance, boolean front) {
        SignText signText = signBlockEntity.getText(front);
        String[] lines = text.split("/");
        for (int i = 0, linesLength = lines.length; i < linesLength; i++) {
            String line = lines[i];
            if (line.contains("%d")) {
                line = line.formatted(distance);
            }

            MutableText message = Text.literal(line);
            signText = (signText.withMessage(i, message));
        }
        signBlockEntity.setText(signText, front);
    }

    @Override
    public void setWoodType(Records.WoodAssets assets) {
        this.wood = assets;
    }
}
