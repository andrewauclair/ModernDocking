/**
 * Module for the Modern Docking framework
 */
module modern_docking.api {
	requires java.desktop;
    requires java.logging;

    exports ModernDocking;
	exports ModernDocking.event;
	exports ModernDocking.exception;
	exports ModernDocking.layouts;
	exports ModernDocking.persist;
 exports ModernDocking.settings;
	exports ModernDocking.ui;
    exports ModernDocking.api;
	exports ModernDocking.internal;
}