package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.tridents_accept_sharpness")
public class FeatureTridentsAcceptSharpness extends DataPackFeature {
	public FeatureTridentsAcceptSharpness() {
		super("tridents_accept_sharpness");
	}
}
