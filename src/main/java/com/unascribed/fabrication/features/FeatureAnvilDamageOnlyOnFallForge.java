package com.unascribed.fabrication.features;

import com.unascribed.fabrication.DelegateFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

// TODO
@EligibleIf(configAvailable="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.NEVER)
public class FeatureAnvilDamageOnlyOnFallForge extends DelegateFeature {

	public FeatureAnvilDamageOnlyOnFallForge() {
		super("com.unascribed.fabrication.FeatureAnvilDamageOnlyOnFallForgeImpl");
	}

}
