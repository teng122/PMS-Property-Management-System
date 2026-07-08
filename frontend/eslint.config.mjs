import nextVitals from "eslint-config-next/core-web-vitals";

const config = [
  ...nextVitals,
  {
    ignores: [".next/**", "node_modules/**", "next-env.d.ts"]
  },
  {
    rules: {
      "react/no-unescaped-entities": "off"
    }
  }
];

export default config;
