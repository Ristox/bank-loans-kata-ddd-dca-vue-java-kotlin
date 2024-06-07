interface User {
    id: number;
    firstName: string;
    lastName: string;
}

const SERVER_URL = '/api'

class Server {

    constructor() {}

    async checkHealth(): Promise<any> {
        const response = await fetch(SERVER_URL + '/health')
        return response.json()
    }
}

export default new Server()
