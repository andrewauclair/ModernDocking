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

	exports io.github.andrewauclair.moderndocking;
	exports io.github.andrewauclair.moderndocking.event;
	exports io.github.andrewauclair.moderndocking.exception;
	exports io.github.andrewauclair.moderndocking.layouts;
	exports io.github.andrewauclair.moderndocking.persist;
	exports io.github.andrewauclair.moderndocking.settings;
	exports io.github.andrewauclair.moderndocking.ui;
	exports io.github.andrewauclair.moderndocking.api;

	// export our internal package only to our other extension modules
	exports ModernDocking.internal to modern_docking.ui_ext, modern_docking.single_app, modern_docking.multi_app;
	exports io.github.andrewauclair.moderndocking.internal to modern_docking.ui_ext, modern_docking.single_app, modern_docking.multi_app;
}