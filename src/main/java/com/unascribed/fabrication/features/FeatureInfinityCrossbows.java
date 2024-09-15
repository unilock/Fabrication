package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.infinity_crossbows")
public class FeatureInfinityCrossbows extends DataPackFeature {
	public FeatureInfinityCrossbows() {
		super("infinity_crossbows");
	}
}
