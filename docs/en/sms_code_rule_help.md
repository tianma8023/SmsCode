Custom SMS code rule help
--------

- You can custom SMS code rules when default algorithm of parsing SMS code cannot meet special needs.
- SMS code rule contains 3 parts: company, code keyword and code regular expression.

Note that: 
- **As long as the rules formed by the above three parts can correctly identify the specified verification code SMS, there is no need to stick to the format. That is, there are different ways to define the rule for a certain SMS.**
- For the usage of Regular Expression, you can search on Google by yourself.

Example：
1. 
  ```text
  [Google] Your verification code is 198901
  ```
  - Company can be: `Google`(Recommended), `[Google]` etc. Case insensitive by default.
  - Code keyword can be: `verification code`(Recommended), `code` etc. Case insenditive by default.
  - Code regular expression： `(?<![0-9])[0-9]{6}(?![0-9])`