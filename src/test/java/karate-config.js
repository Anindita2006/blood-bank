// Karate reads this first. It sets the base URL every test uses.
function fn() {
  var port = karate.properties['app.port'] || '8080';
  return { baseUrl: 'http://localhost:' + port };
}
