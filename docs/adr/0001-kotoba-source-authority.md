# ADR 0001: Kotoba is the NRA catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Decision

`src/association_facts.kotoba` is the sole production source. `nra` means the
National Restaurant Association. The history citation retains complete date
`1919-03-13`; ServSafe continues to omit an unverified launch date. Indexed
access preserves governance and food-safety. Unknown values and indexes return
zero or typed option-none; no effects are declared.

CI executes reference semantics, restricted JavaScript, instantiated typed
WebAssembly, and production source-authority checks. Clojure and the JVM are
compiler/test hosts only.

## Consequences

- Complete and absent date states remain distinct.
- The shared initialism does not grant another organization authority.
