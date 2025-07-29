package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;

public class AutocraftingPreviewContainerMenu extends AbstractResourceContainerMenu {
    private final List<AutocraftingRequest> requests;

    private AutocraftingRequest currentRequest;
    @Nullable
    private AutocraftingPreviewListener listener;
    private AutocraftingPreviewStyle style;

    AutocraftingPreviewContainerMenu(final List<AutocraftingRequest> requests) {
        this(null, 0, requests);
    }

    public AutocraftingPreviewContainerMenu(@Nullable final MenuType<?> type,
                                            final int syncId,
                                            final List<AutocraftingRequest> requests) {
        super(type, syncId);
        this.requests = new ArrayList<>(requests);
        this.currentRequest = requests.getFirst();
        this.style = Platform.INSTANCE.getConfig().getAutocraftingPreviewStyle();
        final ResourceContainer resourceContainer = ResourceContainerImpl.createForFilter(1);
        resourceContainer.set(0, new ResourceAmount(requests.getFirst().getResource(), 1));
        addSlot(new DisabledResourceSlot(
            resourceContainer,
            0,
            Component.empty(),
            157,
            48,
            ResourceSlotType.FILTER
        ));
    }

    void setListener(final AutocraftingPreviewListener listener) {
        this.listener = listener;
    }

    AutocraftingPreviewStyle getStyle() {
        return style;
    }

    AutocraftingPreviewStyle toggleStyle(final double amount) {
        style = style.next();
        Platform.INSTANCE.getConfig().setAutocraftingPreviewStyle(style);
        currentRequest.clearPreview();
        if (currentRequest.sendPreviewRequest(amount, style) && listener != null) {
            listener.previewChanged(null, null);
        }
        return style;
    }

    List<AutocraftingRequest> getRequests() {
        return requests;
    }

    AutocraftingRequest getCurrentRequest() {
        return currentRequest;
    }

    void setCurrentRequest(final AutocraftingRequest request) {
        this.currentRequest = request;
        if (listener != null) {
            listener.requestChanged(request);
        }
    }

    void amountChanged(final double amount) {
        if (currentRequest.sendPreviewRequest(amount, style) && listener != null) {
            listener.previewChanged(null, null);
        }
    }

    public void previewResponseReceived(final UUID id, final Preview preview) {
        if (!currentRequest.getId().equals(id)) {
            return;
        }
        currentRequest.previewResponseReceived(preview);
        if (listener != null) {
            listener.previewChanged(preview, null);
        }
    }

    public void previewResponseReceived(final UUID id, final TreePreview preview) {
        if (!currentRequest.getId().equals(id)) {
            return;
        }
        currentRequest.previewResponseReceived(preview);
        if (listener != null) {
            listener.previewChanged(null, preview);
        }
    }

    void loadCurrentRequest() {
        currentRequest.clearPreview();
        if (listener != null) {
            listener.requestChanged(currentRequest);
        }
    }

    void sendRequest(final double amount, final boolean notify) {
        currentRequest.sendRequest(amount, notify);
    }

    public void responseReceived(final UUID id, final boolean success) {
        if (!currentRequest.getId().equals(id) || !success) {
            return;
        }
        requests.remove(currentRequest);
        final boolean last = requests.isEmpty();
        if (listener != null) {
            listener.requestRemoved(currentRequest, last);
        }
        if (!last) {
            setCurrentRequest(requests.getFirst());
        }
    }

    public void maxAmountResponseReceived(final long maxAmount) {
        if (listener == null) {
            return;
        }
        if (currentRequest.getResource() instanceof PlatformResourceKey resource) {
            listener.maxAmountReceived(resource.getResourceType().getDisplayAmount(maxAmount));
        }
    }

    void requestMaxAmount() {
        if (currentRequest.getResource() instanceof PlatformResourceKey resource) {
            C2SPackets.sendAutocraftingPreviewMaxAmountRequest(resource);
        }
    }

    boolean isNotify() {
        return Platform.INSTANCE.getConfig().isAutocraftingNotification();
    }

    void setNotify(final boolean notify) {
        Platform.INSTANCE.getConfig().setAutocraftingNotification(notify);
    }

    double getMinAmount() {
        if (currentRequest.getResource() instanceof PlatformResourceKey platformResource) {
            return platformResource.getResourceType().getDisplayAmount(1);
        }
        return 1D;
    }

    void sendCancelRequest() {
        C2SPackets.sendAutocraftingPreviewCancelRequest();
    }
}
