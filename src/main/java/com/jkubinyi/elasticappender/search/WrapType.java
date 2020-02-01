package com.jkubinyi.elasticappender.search;

public enum WrapType {
	/** Sums all field scores together and uses it for filtering the results. <b>BOOL</b> wrapping query. */
	sum,

	/** Counts each field score and uses the highest for filtering the results. <b>DIS_MAX</b> wrapping query. */
	max
}
