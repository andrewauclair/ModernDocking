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

	// export our internal package only to our other extension modules
	exports ModernDocking.internal to modern_docking.ui_ext, modern_docking.single_app, modern_docking.multi_app;
}