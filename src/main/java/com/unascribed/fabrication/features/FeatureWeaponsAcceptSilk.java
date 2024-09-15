package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.weapons_accept_silk")
public class FeatureWeaponsAcceptSilk extends DataPackFeature {
	public FeatureWeaponsAcceptSilk() {
		super("weapons_accept_silk");
	}
}
