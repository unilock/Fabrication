package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.DataPackFeature;

@EligibleIf(configAvailable = "*.bedrock_impaling")
public class FeatureBedrockImpaling extends DataPackFeature {
	public FeatureBedrockImpaling(String path) {
		super("bedrock_impaling");
	}
}
