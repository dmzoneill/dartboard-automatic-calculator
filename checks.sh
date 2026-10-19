#!/usr/bin/env bash
# Vendored third-party JH Labs Java Image Filters (Jerry Huxtable, Apache-2.0).
# Same tree as the root .ignore entry. Super-linter reads FILTER_REGEX_EXCLUDE
# when building the file list; jscpd reads .ignore on its own.
if [[ -n "${GITHUB_ENV:-}" ]]; then
	echo 'FILTER_REGEX_EXCLUDE=dartboardextreme/com/jhlabs' >>"${GITHUB_ENV}"
fi
exit 0
