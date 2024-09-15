package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.infinity_mending")
public class FeatureInfinityMending extends DataPackFeature {
	public FeatureInfinityMending() {
		super("infinity_mending");
	}
}
