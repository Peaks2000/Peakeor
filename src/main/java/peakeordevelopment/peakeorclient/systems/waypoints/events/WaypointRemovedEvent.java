/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.systems.waypoints.events;

import peakeordevelopment.peakeorclient.systems.waypoints.Waypoint;

public record WaypointRemovedEvent(Waypoint waypoint) {
}
