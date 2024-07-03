/**
 * Module for the Modern Docking framework
 */
module modern_docking.single_app {
	requires modern_docking.api;
	requires java.desktop;

	exports ModernDocking.app;
	exports io.github.andrewauclair.moderndocking.app;
}