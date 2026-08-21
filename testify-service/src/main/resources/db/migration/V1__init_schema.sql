CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE folders (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    parent_folder_id UUID,
    name VARCHAR(100) NOT NULL,
    depth_level INTEGER NOT NULL CHECK (depth_level IN (1, 2, 3)),
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_folder_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_folder_parent FOREIGN KEY (parent_folder_id) REFERENCES folders(id)
);

CREATE INDEX idx_folders_workspace_id ON folders(workspace_id);
CREATE INDEX idx_folders_parent_id ON folders(parent_folder_id);

CREATE TABLE rule_configs (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    folder_id UUID,
    config_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    rules JSONB NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rule_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_rule_folder FOREIGN KEY (folder_id) REFERENCES folders(id)
);

CREATE INDEX idx_rule_configs_workspace_id ON rule_configs(workspace_id);

CREATE TABLE field_configs (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    contains_keywords JSONB,
    default_regex VARCHAR(255),
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_field_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE INDEX idx_field_configs_workspace_id ON field_configs(workspace_id);

CREATE TABLE requests (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    folder_id UUID,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    method VARCHAR(10) NOT NULL DEFAULT 'GET',
    headers JSONB,
    body_template TEXT,
    default_rule_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_req_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_req_folder FOREIGN KEY (folder_id) REFERENCES folders(id),
    CONSTRAINT fk_req_default_rule FOREIGN KEY (default_rule_id) REFERENCES rule_configs(id)
);

CREATE INDEX idx_requests_workspace_id ON requests(workspace_id);
