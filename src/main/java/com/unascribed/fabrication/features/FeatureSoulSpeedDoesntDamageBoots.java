package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.soul_speed_doesnt_damage_boots")
public class FeatureSoulSpeedDoesntDamageBoots extends DataPackFeature {
	public FeatureSoulSpeedDoesntDamageBoots() {
		super("soul_speed_doesnt_damage_boots");
	}
}
