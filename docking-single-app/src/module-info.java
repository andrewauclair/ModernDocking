/**
 * Module for the Modern Docking framework
 */
module modern_docking.single_app {
	requires modern_docking.api;
	requires java.desktop;
    requires java.logging;

    exports io.github.andrewauclair.moderndocking.app;
}