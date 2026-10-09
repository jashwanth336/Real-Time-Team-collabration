-- PostgreSQL reference schema. In development Hibernate creates these tables.
CREATE TABLE users (
	id BIGSERIAL PRIMARY KEY,
	email VARCHAR(120) UNIQUE NOT NULL,
	username VARCHAR(40) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	display_name VARCHAR(80) NOT NULL,
	role VARCHAR(20) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE workspaces (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	description VARCHAR(500),
	owner_id BIGINT NOT NULL REFERENCES users(id),
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE workspace_members (
	workspace_id BIGINT REFERENCES workspaces(id),
	user_id BIGINT REFERENCES users(id),
	PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE projects (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(120) NOT NULL,
	description VARCHAR(1000),
	status VARCHAR(30) NOT NULL,
	workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE tasks (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(160) NOT NULL,
	description VARCHAR(2000),
	status VARCHAR(30) NOT NULL,
	priority VARCHAR(30) NOT NULL,
	due_date DATE,
	project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
	assignee_id BIGINT REFERENCES users(id),
	reporter_id BIGINT NOT NULL REFERENCES users(id),
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE comments (
	id BIGSERIAL PRIMARY KEY,
	body VARCHAR(2000) NOT NULL,
	task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
	author_id BIGINT NOT NULL REFERENCES users(id),
	created_at TIMESTAMPTZ NOT NULL
);
