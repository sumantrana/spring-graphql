java -jar rsc-0.9.1.jar \
  --request `# Message exchange pattern request, subscription etc.` \
  --route=graphql `# Graphql mapping endpoint configured in the Spring application.properties file.` \
  --dataMimeType="application/graphql+json"  `# Content type. The type is same as when using HTTP` \
  --data='{"query": "query { greeting { greeting } }" }'  `# Request data to be sent to the server` \
  --debug tcp://localhost:9191 `# Endpoint on which the server is listening for Rsocket requests.`