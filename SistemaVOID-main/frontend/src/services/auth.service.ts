export interface User {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export interface AuthResponse {
  message: string;
  data: {
    token: string;
    user: User;
  };
}

const API_URL = process.env.NEXT_PUBLIC_API_URL 
  || (typeof window !== 'undefined' 
      ? `http://${window.location.hostname}:8080/api/auth` 
      : 'http://localhost:8080/api/auth');

export const authService = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      let errorMessage = 'Credenciales inválidas o error en el servidor';
      try {
        const error = await res.json();
        errorMessage = error.error || error.message || errorMessage;
      } catch {
        // Ignorar error al parsear cuerpo no json
      }
      throw new Error(errorMessage);
    }

    const resData: any = await res.json();
    const token = resData.data?.token || resData.token;
    const user = resData.data?.user || resData.user;

    const data: AuthResponse = {
      message: resData.message || 'Login exitoso',
      data: {
        token,
        user
      }
    };

    if (typeof document !== 'undefined' && token) {
      document.cookie = `void_token=${token}; path=/; max-age=${7 * 24 * 60 * 60}`;
    }
    if (typeof localStorage !== 'undefined' && user) {
      localStorage.setItem('void_user', JSON.stringify(user));
    }

    return data;
  },

  async register(email: string, password: string, fullName: string): Promise<AuthResponse> {
    const res = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, fullName }),
    });

    if (!res.ok) {
      let errorMessage = 'Error al registrar usuario';
      try {
        const error = await res.json();
        errorMessage = error.error || error.message || errorMessage;
      } catch {
        // Ignorar error al parsear cuerpo no json
      }
      throw new Error(errorMessage);
    }

    return this.login(email, password);
  },

  logout() {
    document.cookie = 'void_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    localStorage.removeItem('void_user');
    window.location.href = '/login';
  },

  getCurrentUser(): User | null {
    if (typeof window === 'undefined') return null;
    const user = localStorage.getItem('void_user');
    return user ? JSON.parse(user) : null;
  }
};
