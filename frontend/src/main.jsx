import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import "./styles.css";

const API = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const Auth = createContext();

function readStoredUser() {
  try {
    const raw = localStorage.getItem("syncspace_user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function getDisplayName(user) {
  return user?.displayName || user?.username || user?.email || "User";
}

function getUserInitials(user) {
  const name = String(getDisplayName(user)).trim();
  return name ? name.charAt(0).toUpperCase() : "?";
}

function getDisplayNameFromActor(actor) {
  return getDisplayName(actor || null);
}

async function request(path, options = {}) {
  const token = localStorage.getItem("syncspace_token");
  const res = await fetch(`${API}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...options,
  });
  if (res.status === 204) return null;
  const data = await res
    .json()
    .catch(() => ({ message: "Unexpected server response" }));
  if (!res.ok) throw new Error(data.message || "Request failed");
  return data;
}
function AuthProvider({ children }) {
  const [user, setUser] = useState(() => readStoredUser());
  const login = useCallback((data) => {
    localStorage.setItem("syncspace_token", data.token);
    localStorage.setItem("syncspace_user", JSON.stringify(data.user));
    setUser(data.user);
  }, []);
  const logout = useCallback(() => {
    localStorage.removeItem("syncspace_token");
    localStorage.removeItem("syncspace_user");
    setUser(null);
  }, []);
  const authValue = useMemo(
    () => ({ user, login, logout }),
    [user, login, logout],
  );
  return <Auth.Provider value={authValue}>{children}</Auth.Provider>;
}
function useAuth() {
  return useContext(Auth);
}
function AuthPage() {
  const { login } = useAuth(),
    nav = useNavigate();
  const [register, setRegister] = useState(false),
    [form, setForm] = useState({
      email: "demo@syncspace.dev",
      password: "Password123!",
      username: "",
      displayName: "",
    }),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const body = register
        ? form
        : { email: form.email, password: form.password };
      login(
        await request(`/auth/${register ? "register" : "login"}`, {
          method: "POST",
          body: JSON.stringify(body),
        }),
      );
      nav("/");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };
  let submitLabel = register ? "Create account" : "Sign in";
  if (busy) submitLabel = "Please wait…";
  return (
    <main className="auth">
      <section className="brand-panel">
        <div className="logo">S</div>
        <h1>Move great work forward.</h1>
        <p>
          SyncSpace gives your team one calm, clear place to plan projects and
          turn ideas into done.
        </p>
        <div className="feature">✓ Organize workspaces and projects</div>
        <div className="feature">
          ✓ Make progress visible with Kanban boards
        </div>
        <div className="feature">✓ Keep context with tasks and comments</div>
      </section>
      <section className="auth-card">
        <p className="eyebrow">WELCOME TO SYNCSPACE</p>
        <h2>{register ? "Create your account" : "Welcome back"}</h2>
        <p className="muted">
          {register
            ? "Start collaborating in a few seconds."
            : "Sign in to continue to your workspace."}
        </p>
        <form onSubmit={submit}>
          {register && (
            <>
              <Field
                label="Display name"
                value={form.displayName}
                onChange={(v) => setForm({ ...form, displayName: v })}
              />
              <Field
                label="Username"
                value={form.username}
                onChange={(v) => setForm({ ...form, username: v })}
              />
            </>
          )}
          <Field
            label="Email"
            type="email"
            value={form.email}
            onChange={(v) => setForm({ ...form, email: v })}
          />
          <Field
            label="Password"
            type="password"
            value={form.password}
            onChange={(v) => setForm({ ...form, password: v })}
          />
          {error && <p className="error">{error}</p>}
          <button type="submit" className="primary wide" disabled={busy}>
            {submitLabel}
          </button>
        </form>
        <p className="switch">
          {register ? "Already have an account?" : "New to SyncSpace?"}{" "}
          <button
            type="button"
            onClick={() => {
              setRegister(!register);
              setError("");
            }}
          >
            {register ? "Sign in" : "Create an account"}
          </button>
        </p>
      </section>
    </main>
  );
}
function Field({ label, type = "text", value, onChange }) {
  return (
    <label className="field">
      {label}
      <input
        required
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </label>
  );
}
function Shell({ children }) {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  if (!user) {
    return <>{children}</>;
  }

  const displayName = getDisplayName(user);
  const initials = getUserInitials(user);

  return (
    <div className="app">
      <aside>
        <div className="wordmark">
          <span>✦</span> SyncSpace
        </div>
        <button type="button" className="nav active" onClick={() => nav("/")}>
          ▦ Overview
        </button>
        <div className="side-label">WORKSPACE</div>
        <button type="button" className="nav" onClick={() => nav("/")}>
          ⌂ All workspaces
        </button>
        <div className="userbox">
          <div className="avatar">{initials}</div>
          <div>
            <b>{displayName}</b>
            <small>{user.email || "No email"}</small>
          </div>
          <button type="button" title="Sign out" onClick={logout}>
            ↪
          </button>
        </div>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
function Dashboard() {
  const [spaces, setSpaces] = useState([]),
    [creating, setCreating] = useState(false),
    [name, setName] = useState(""),
    [error, setError] = useState("");
  const nav = useNavigate();
  const load = useCallback(async () => {
    try {
      const data = await request("/workspaces");
      setSpaces(data || []);
      setError("");
    } catch (e) {
      setError(e.message);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);
  const create = async (e) => {
    e.preventDefault();
    try {
      const w = await request("/workspaces", {
        method: "POST",
        body: JSON.stringify({ name, description: "" }),
      });
      nav(`/workspaces/${w.id}`);
    } catch (e) {
      setError(e.message);
    }
  };
  return (
    <Shell>
      <header className="page-header">
        <div>
          <p className="eyebrow">YOUR SPACE</p>
          <h1>Good morning.</h1>
          <p className="muted">
            Pick up where you left off or start something new.
          </p>
        </div>
        <button
          type="button"
          className="primary"
          onClick={() => setCreating(true)}
        >
          + New workspace
        </button>
      </header>
      {error && <p className="error">{error}</p>}
      {creating && (
        <form className="create-bar" onSubmit={create}>
          <input
            placeholder="Workspace name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <button type="submit" className="primary">
            Create
          </button>
          <button type="button" onClick={() => setCreating(false)}>
            Cancel
          </button>
        </form>
      )}
      <section>
        <div className="section-title">
          <h2>Your workspaces</h2>
          <span>{spaces.length} total</span>
        </div>
        <div className="cards">
          {spaces.map((w) => (
            <button
              type="button"
              className="workspace-card"
              key={w.id}
              onClick={() => nav(`/workspaces/${w.id}`)}
            >
              <div className="workspace-icon">{w.name[0]}</div>
              <h3>{w.name}</h3>
              <p>{w.description || "No description yet."}</p>
              <footer>
                <span>
                  {w.members?.length || 1} member
                  {(w.members?.length || 1) !== 1 ? "s" : ""}
                </span>
                <span>Open →</span>
              </footer>
            </button>
          ))}
          {!spaces.length && (
            <Empty text="No workspaces yet. Create one to begin." />
          )}
        </div>
      </section>
    </Shell>
  );
}
function WorkspacePage() {
  const { id } = useParams(),
    nav = useNavigate();
  const [data, setData] = useState(null),
    [newName, setNewName] = useState(""),
    [error, setError] = useState("");
  const load = useCallback(async () => {
    try {
      const nextData = await request(`/workspaces/${id}`);
      setData(nextData);
      setError("");
    } catch (e) {
      setError(e.message);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);
  if (!data)
    return (
      <Shell>
        <p>Loading workspace…</p>
      </Shell>
    );
  const makeProject = async (e) => {
    e.preventDefault();
    try {
      const p = await request(`/workspaces/${id}/projects`, {
        method: "POST",
        body: JSON.stringify({
          name: newName,
          description: "",
          status: "ACTIVE",
        }),
      });
      nav(`/projects/${p.id}`);
    } catch (e) {
      setError(e.message);
    }
  };
  return (
    <Shell>
      <button className="back" onClick={() => nav("/")}>
        ← All workspaces
      </button>
      <header className="page-header compact">
        <div>
          <p className="eyebrow">WORKSPACE</p>
          <h1>{data.workspace.name}</h1>
          <p className="muted">
            {data.workspace.description || "Plan, track, and deliver together."}
          </p>
        </div>
        <div className="metric">
          <b>{data.taskCount}</b>
          <span>tasks</span>
        </div>
      </header>
      {error && <p className="error">{error}</p>}
      <section className="split">
        <div>
          <div className="section-title">
            <h2>Projects</h2>
          </div>
          <div className="project-list">
            {data.projects.map((p) => (
              <button
                type="button"
                key={p.id}
                className="project-row"
                onClick={() => nav(`/projects/${p.id}`)}
              >
                <span className="project-dot"></span>
                <div>
                  <b>{p.name}</b>
                  <p>{p.description || "No description"}</p>
                </div>
                <span className="pill">{p.status.replace("_", " ")}</span>
                <span>→</span>
              </button>
            ))}
          </div>
          <form className="inline-create" onSubmit={makeProject}>
            <input
              placeholder="New project name"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              required
            />
            <button type="submit" className="primary">
              Add project
            </button>
          </form>
        </div>
        <div>
          <div className="section-title">
            <h2>Recent activity</h2>
          </div>
          <div className="activity">
            {data.activities.map((a) => (
              <div key={a.id}>
                <div className="avatar small">
                  {getUserInitials(a.actor || null)}
                </div>
                <p>
                  <b>{getDisplayNameFromActor(a.actor)}</b> {a.message}
                  <small>{new Date(a.createdAt).toLocaleDateString()}</small>
                </p>
              </div>
            ))}
            {!data.activities.length && (
              <Empty text="Activity will appear here." />
            )}
          </div>
        </div>
      </section>
    </Shell>
  );
}
function ProjectPage() {
  const { id } = useParams(),
    nav = useNavigate();
  const [data, setData] = useState(null),
    [draft, setDraft] = useState(""),
    [error, setError] = useState("");
  const load = useCallback(async () => {
    try {
      const nextData = await request(`/projects/${id}`);
      setData(nextData);
      setError("");
    } catch (e) {
      setError(e.message);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);
  if (!data)
    return (
      <Shell>
        <p>Loading project…</p>
      </Shell>
    );
  const create = async (e) => {
    e.preventDefault();
    try {
      await request(`/projects/${id}/tasks`, {
        method: "POST",
        body: JSON.stringify({
          title: draft,
          description: "",
          status: "TODO",
          priority: "MEDIUM",
        }),
      });
      setDraft("");
      load();
    } catch (e) {
      setError(e.message);
    }
  };
  const move = async (task, status) => {
    try {
      await request(`/tasks/${task.id}`, {
        method: "PUT",
        body: JSON.stringify({
          title: task.title,
          description: task.description || "",
          status,
          priority: task.priority,
          dueDate: task.dueDate,
          assigneeId: task.assignee?.id || null,
        }),
      });
      load();
    } catch (e) {
      setError(e.message);
    }
  };
  const columns = [
    ["TODO", "To do"],
    ["IN_PROGRESS", "In progress"],
    ["DONE", "Done"],
  ];
  return (
    <Shell>
      <button
        className="back"
        onClick={() => nav(`/workspaces/${data.project.workspace.id}`)}
      >
        ← {data.project.workspace.name}
      </button>
      <header className="page-header compact">
        <div>
          <p className="eyebrow">PROJECT BOARD</p>
          <h1>{data.project.name}</h1>
          <p className="muted">
            {data.project.description ||
              "Track every piece of work from idea to done."}
          </p>
        </div>
        <span className="pill active-pill">{data.project.status}</span>
      </header>
      {error && <p className="error">{error}</p>}
      <form className="task-create" onSubmit={create}>
        <input
          placeholder="What needs to be done?"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          required
        />
        <button type="submit" className="primary">
          + Add task
        </button>
      </form>
      <div className="board">
        {columns.map(([key, label]) => {
          const tasks = data.tasks.filter((t) => t.status === key);
          return (
            <section className="column" key={key}>
              <h3>
                <span className={`status-dot ${key.toLowerCase()}`}></span>
                {label}
                <small>{tasks.length}</small>
              </h3>
              {tasks.map((t) => (
                <article className="task-card" key={t.id}>
                  <div className="task-top">
                    <span className={`priority ${t.priority.toLowerCase()}`}>
                      {t.priority}
                    </span>
                    <button type="button" onClick={() => nav(`/tasks/${t.id}`)}>
                      •••
                    </button>
                  </div>
                  <h4>{t.title}</h4>
                  {t.description && <p>{t.description}</p>}
                  <footer>
                    {t.dueDate && <span>◷ {t.dueDate}</span>}
                    <span className="avatar small">
                      {t.assignee?.displayName?.[0] || "?"}
                    </span>
                  </footer>
                  <div className="moves">
                    {key !== "TODO" && (
                      <button
                        type="button"
                        onClick={() =>
                          move(t, key === "DONE" ? "IN_PROGRESS" : "TODO")
                        }
                      >
                        ←
                      </button>
                    )}
                    {key !== "DONE" && (
                      <button
                        type="button"
                        onClick={() =>
                          move(t, key === "TODO" ? "IN_PROGRESS" : "DONE")
                        }
                      >
                        →
                      </button>
                    )}
                  </div>
                </article>
              ))}
              {!tasks.length && <div className="dropzone">No tasks here</div>}
            </section>
          );
        })}
      </div>
    </Shell>
  );
}
function TaskPage() {
  const { id } = useParams(),
    nav = useNavigate();
  const [data, setData] = useState(null),
    [comment, setComment] = useState(""),
    [error, setError] = useState("");
  const load = useCallback(async () => {
    try {
      const nextData = await request(`/tasks/${id}`);
      setData(nextData);
      setError("");
    } catch (e) {
      setError(e.message);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);
  if (!data)
    return (
      <Shell>
        <p>Loading task…</p>
      </Shell>
    );
  const send = async (e) => {
    e.preventDefault();
    try {
      await request(`/tasks/${id}/comments`, {
        method: "POST",
        body: JSON.stringify({ body: comment }),
      });
      setComment("");
      load();
    } catch (e) {
      setError(e.message);
    }
  };
  const t = data.task;
  return (
    <Shell>
      <button
        type="button"
        className="back"
        onClick={() => nav(`/projects/${t.project.id}`)}
      >
        ← Back to board
      </button>
      <div className="task-detail">
        <section>
          <p className="eyebrow">TASK · {t.status.replace("_", " ")}</p>
          <h1>{t.title}</h1>
          <p className="description">
            {t.description || "No description has been added."}
          </p>
          <div className="detail-grid">
            <div>
              <span>Priority</span>
              <b className={`priority ${t.priority.toLowerCase()}`}>
                {t.priority}
              </b>
            </div>
            <div>
              <span>Assignee</span>
              <b>{t.assignee?.displayName || "Unassigned"}</b>
            </div>
            <div>
              <span>Due date</span>
              <b>{t.dueDate || "No due date"}</b>
            </div>
          </div>
        </section>
        <section className="comments">
          <h2>
            Comments <small>{data.comments.length}</small>
          </h2>
          {data.comments.map((c) => (
            <div className="comment" key={c.id}>
              <div className="avatar small">
                {getUserInitials(c.author || null)}
              </div>
              <div>
                <b>{getDisplayNameFromActor(c.author)}</b>
                <small>{new Date(c.createdAt).toLocaleString()}</small>
                <p>{c.body}</p>
              </div>
            </div>
          ))}
          <form onSubmit={send}>
            <textarea
              required
              placeholder="Write a comment…"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />
            {error && <p className="error">{error}</p>}
            <button type="submit" className="primary">
              Post comment
            </button>
          </form>
        </section>
      </div>
    </Shell>
  );
}
function Empty({ text }) {
  return <div className="empty">{text}</div>;
}
function App() {
  const { user } = useAuth();
  return (
    <Routes>
      {!user ? (
        <Route path="*" element={<AuthPage />} />
      ) : (
        <>
          <Route path="/" element={<Dashboard />} />
          <Route path="/workspaces/:id" element={<WorkspacePage />} />
          <Route path="/projects/:id" element={<ProjectPage />} />
          <Route path="/tasks/:id" element={<TaskPage />} />
          <Route path="*" element={<Navigate to="/" />} />
        </>
      )}
    </Routes>
  );
}
createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
