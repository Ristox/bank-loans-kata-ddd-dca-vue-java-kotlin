const apiUrl = `${Cypress.env("apiUrl")}`

describe('Server Health Check', () => {

  it('calls /health endpoint', () => {
    cy.request({
      failOnStatusCode: false,
      method: 'GET',
      url: `${apiUrl}/health`,
    }).then((response) => {
      expect(response.status)
        .to
        .eq(200)
      expect(response.body.status)
        .to
        .eq('OK')
    })
  })
})

describe('UI first page', () => {
  it('Should display', () => {
    cy.visit('/')
  })
})
