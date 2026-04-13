package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractYesNoSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class DestructorPickupItemsSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "destructor.pickup_items");
    private static final Identifier YES = createIdentifier("widget/side_button/destructor_pickup_items/yes");
    private static final Identifier NO = createIdentifier("widget/side_button/destructor_pickup_items/no");

    DestructorPickupItemsSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE, YES, NO);
    }
}
