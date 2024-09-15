package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.tridents_accept_power")
public class FeatureTridentsAcceptPower extends DataPackFeature {
	public FeatureTridentsAcceptPower() {
		super("tridents_accept_power");
	}
}
