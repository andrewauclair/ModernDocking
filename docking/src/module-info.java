/**
 * Module for the Modern Docking framework
 */
module modern_docking {
	requires java.desktop;
    requires java.logging;

    exports ModernDocking;
	exports ModernDocking.event;
	exports ModernDocking.exception;
	exports ModernDocking.layouts;
	exports ModernDocking.persist;
	exports ModernDocking.ui;
}