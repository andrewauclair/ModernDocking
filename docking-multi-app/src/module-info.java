/**
 * Module for the Modern Docking framework
 */
module modern_docking.multi_app {
	requires modern_docking.api;
	requires java.desktop;
    requires java.logging;

    exports ModernDocking.app;
}