package com.unascribed.fabrication.interfaces;

import com.unascribed.fabrication.util.ByteBufCustomPayload;

public interface ByteBufCustomPayloadReceiver {
	void fabrication$onCustomPayload(ByteBufCustomPayload payload);
}
