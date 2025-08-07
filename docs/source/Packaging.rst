#############
Packaging
#############

.. _FlatLaf: https://www.formdev.com/flatlaf

Modern Docking is provided on Maven Central as a collection of packages.

Most applications will use ``modern-docking-api`` and ``modern-docking-single-app``. There is an additional UI package (:doc:`Modern-Docking-UI`) that adds special support for the `FlatLaf`_ look and feel library.

In addition to the above packages, there is a package for applications that launch multiple instances in a single JVM, which is how IntelliJ IDEA works. This package is available as ``modern-docking-multi-app`` and replaces ``modern-docking-single-app`` for the multi-app in JVM use case.