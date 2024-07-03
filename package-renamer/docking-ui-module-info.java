/**
 * Module for the Modern Docking framework
 */
module modern_docking.ui_ext {
	requires modern_docking.api;
	requires java.desktop;
	requires com.formdev.flatlaf.extras;

	exports ModernDocking.ext.ui;
	exports io.github.andrewauclair.moderndocking.ext.ui;
}