LLM used : Cloude Snonet 4.6 


Login Prompt: 

You are a QA engineer and you have to create testcases  for a login module covering  Valid login, invalid credentials, forgot password, session expiry, brute-force lockout. and create a json file for that testcases.


what didn't work first time :

llm hallucinate  the CAPTCHA challenge in the application afer 3 fails attempts

So i give another prompt :
remove the CAPTCHA challenge it in not a part of our application.


Dashboard Prompt : 
You are a QA engineer you have to create the testcases for Dashboard module of the application and  covering all the functionality like  Widget loading, data accuracy, filter/sort behavior, responsive layout, permission-based visibility  and create a json fie for it.


what didn't work first time :


llm create the repetitive testcases and less optimized testcases By the new prompt the testcase connt reduces from 40 to 17 and testcase looks more optimized.

So i give another prompt :
the  testcases which are repetitive try to cover the functionality in less testcases focus on quality not counts


REST API Prompt:
You are a QA engineer you have to create the testcases for backend APIs the testcases should be Optimized and covering  exactly these scenarios  Auth token validation, CRUD operations, error handling (4xx/5xx), rate limiting, schema validation .also create json file for these testcases.  

This promt works pretty will and generated good testcases .
