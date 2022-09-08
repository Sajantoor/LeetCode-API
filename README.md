# Unofficial LeetCode API

Wraps common LeetCode APIs in one place such as submitting, get questions by category, difficulty, etc.

## Usage

-   See [swagger.json](swagger.json)
-   [Swagger UI](https://petstore.swagger.io/?_ga=2.166651710.1040648702.1662666555-1845892988.1661718828)

Certain APIs require `X-CSRF-Token` and `Session` headers for authentication.

Login to your LeetCode account and copy the data from the cookie. For Chrome go to
`chrome://settings/cookies/detail?site=leetcode.com` and use the `X-CSRF-Token` and `LEETCODE_SESSION` values for these headers, respectively.
These tokens remain valid for ~1 week.

## Contributing

Pull requests for new APIs and features are welcome!
