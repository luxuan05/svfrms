package org.example.svfrms.config;

/** Which crop grows in which chamber. */
public record ChamberAssignment(String chamberId, String crop) {}
