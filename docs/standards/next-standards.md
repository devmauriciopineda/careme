---
description: Frontend development standards, best practices, and conventions for the React application including component patterns, state management, UI/UX guidelines, and testing practices
globs: ["frontend/app/**/*.{ts,tsx}", "frontend/src/**/*.{ts,tsx}", "frontend/e2e/playwright/**/*.{ts,tsx}", "frontend/tsconfig.json", "frontend/playwright.config.ts", "frontend/vitest.config.ts", "frontend/package.json"]
alwaysApply: true
---

# Frontend Project Configuration and Best Practices

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
    - [Core Technologies](#core-technologies)
    - [UI Framework](#ui-framework)
    - [Rendering and Delivery Patterns](#rendering-and-delivery-patterns)
    - [State Management & Data Flow](#state-management--data-flow)
    - [Testing Framework](#testing-framework)
    - [Development Tools](#development-tools)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
    - [Naming Conventions](#naming-conventions)
    - [Component Conventions](#component-conventions)
    - [State Management](#state-management)
    - [Service Layer Architecture](#service-layer-architecture)
- [UI/UX Standards](#uiux-standards)
    - [Tailwind and Component Libraries](#tailwind-and-component-libraries)
    - [Form Handling](#form-handling)
    - [Navigation Patterns](#navigation-patterns)
    - [Accessibility](#accessibility)
- [Testing Standards](#testing-standards)
    - [End-to-End Testing with Playwright](#end-to-end-testing-with-playwright)
    - [Test Organization](#test-organization)
- [Configuration Standards](#configuration-standards)
    - [TypeScript Configuration](#typescript-configuration)
    - [ESLint Configuration](#eslint-configuration)
    - [Environment Configuration](#environment-configuration)
- [Performance Best Practices](#performance-best-practices)
    - [Component Optimization](#component-optimization)
    - [Bundle Optimization](#bundle-optimization)
    - [API Efficiency](#api-efficiency)
- [Development Workflow](#development-workflow)
    - [Git Workflow](#git-workflow)
    - [Development Scripts](#development-scripts)
    - [Code Quality](#code-quality)
- [Migration Strategy](#migration-strategy)
    - [TypeScript Migration](#typescript-migration)
    - [Component Modernization](#component-modernization)

---

## Overview

This document outlines the best practices, conventions, and standards used in the frontend application. These practices ensure code consistency, maintainability, and optimal development experience.

## Technology Stack

### Core Technologies
- **React 19**: Modern React with Server Components support and improved concurrent rendering
- **TypeScript 5.x**: Required by default for type safety and maintainability
- **Next.js 16**: Standard React framework for all frontend applications in this project

### UI Framework
- **Tailwind CSS**: Primary utility-first styling system
- **shadcn/ui**: Standard component system
- **Modern CSS features**: CSS variables, nesting, container queries, `:has()`, `color-mix()`, and subgrid

### Rendering and Delivery Patterns
- **Next.js App Router with Server Components + SSR + Streaming**: Mandatory rendering architecture

### State Management & Data Flow
- **React Hooks**: useState, useReducer, and custom hooks for local UI state
- **TanStack Query**: Standard for server state, caching, retries, and invalidation
- **Zustand**: Standard global state solution where cross-feature client state is required
- **Server Components + Server Actions**: Required default for data loading and mutations in Next.js apps

### Testing Framework
- **Vitest**: Default unit and integration test runner
- **React Testing Library**: Component and interaction testing
- **Playwright**: Primary end-to-end testing framework

### Development Tools
- **ESLint**: Code linting with React-specific rules
- **TypeScript**: Static type checking
- **Storybook**: Component-driven development and visual documentation
- **Design tokens**: Shared visual primitives across apps and component libraries
- **AI-assisted development tools**: GitHub Copilot is the standard assistant for implementation and review

## Project Structure

```
frontend/
├── app/                   # Next.js App Router (routes, layouts, server components)
├── public/                # Static assets
├── src/
│   ├── components/        # Reusable UI components
│   ├── features/          # Domain-oriented feature modules
│   ├── services/          # API service layer
│   ├── hooks/             # Shared custom hooks
│   ├── state/             # Global Zustand stores
│   ├── assets/            # Images, fonts, static resources
│   └── lib/               # Utilities, schema validation, and query clients
├── e2e/
│   └── playwright/        # End-to-end test files
├── .storybook/            # Storybook configuration
├── package.json           # Dependencies and scripts
├── tsconfig.json          # TypeScript configuration
├── next.config.ts         # Next.js configuration
├── playwright.config.ts # Playwright configuration
└── vitest.config.ts       # Vitest configuration
```

## Coding Standards

### Naming Conventions

- **Component Naming**: Use PascalCase for React components (e.g., `CandidateCard`, `PositionDetails`, `RecruiterDashboard`)
- **Variable Naming**: Use camelCase for variables and functions (e.g., `candidateId`, `handleSubmit`, `fetchPositions`)
- **Constants Naming**: Use UPPER_SNAKE_CASE for constants (e.g., `MAX_CANDIDATES_PER_PAGE`, `API_BASE_URL`)
- **Type/Interface Naming**: Use PascalCase for types and interfaces (e.g., `CandidateData`, `PositionProps`, `ICandidateService`)
- **File Naming**: Use PascalCase for component files (e.g., `CandidateCard.tsx`, `PositionDetails.tsx`) and camelCase for utility files (e.g., `candidateService.ts`, `apiUtils.ts`)
- **CSS Class Naming**: Use kebab-case for CSS classes (e.g., `candidate-card`, `position-details`)
- **Hook Naming**: Use camelCase starting with "use" prefix (e.g., `useCandidate`, `usePositionData`, `useFormValidation`)

**Examples:**

```typescript
// Good: All in English
import React, { useState, useEffect } from 'react';

type CandidateCardProps = {
    candidate: Candidate;
    index: number;
    onClick: (candidate: Candidate) => void;
};

const CandidateCard: React.FC<CandidateCardProps> = ({ candidate, index, onClick }) => {
    const [isLoading, setIsLoading] = useState(false);
    
    // Handle candidate card click event
    const handleCardClick = () => {
        onClick(candidate);
    };
    
    return (
        <div className="candidate-card" onClick={handleCardClick}>
            {/* Component JSX */}
        </div>
    );
};

// Avoid: Non-English comments or names
const TarjetaCandidato: React.FC<PropsTarjetaCandidato> = ({ candidato, indice, alHacerClic }) => {
    const [estaCargando, setEstaCargando] = useState(false);
    
    // Manejar evento de clic en la tarjeta de candidato
    const manejarClicTarjeta = () => {
        alHacerClic(candidato);
    };
    
    return (
        <div className="tarjeta-candidato" onClick={manejarClicTarjeta}>
            {/* JSX del componente */}
        </div>
    );
};
```

**Error Messages and Console Logs:**

```typescript
// Good: English error messages
catch (error) {
    console.error('Failed to fetch candidates:', error);
    setError('Unable to load candidates. Please try again later.');
}

// Avoid: Non-English messages
catch (error) {
    console.error('Error al obtener candidatos:', error);
    setError('No se pudieron cargar los candidatos. Por favor, inténtelo de nuevo más tarde.');
}
```

**Service Layer Examples:**

```typescript
// Good: English naming in services
export const candidateService = {
    getAllCandidates: async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/candidates`);
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching candidates:', error);
            throw error;
        }
    }
};

// Avoid: Non-English naming
export const servicioCandidatos = {
    obtenerTodosLosCandidatos: async () => {
        try {
            const respuesta = await fetch(`${API_BASE_URL}/candidates`);
            if (!respuesta.ok) {
                throw new Error(`Request failed with status ${respuesta.status}`);
            }
            return await respuesta.json();
        } catch (error) {
            console.error('Error al obtener candidatos:', error);
            throw error;
        }
    }
};
```

### Component Conventions

#### Functional Components
- **Always use functional components** with hooks instead of class components
- Use **TypeScript for all new components**
- Prefer **Server Components by default** in Next.js (use client components only when browser interactivity is required)

```typescript
// Preferred - TypeScript functional component
import React, { useState, useEffect } from 'react';

type Position = {
    id: number;
    title: string;
    status: 'Open' | 'Hired' | 'Closed' | 'Draft';
};

const Positions: React.FC = () => {
    const [positions, setPositions] = useState<Position[]>([]);
    // Component logic
};
```

#### Component Props
- **Define TypeScript interfaces** for component props when using TypeScript
- Use **destructuring** for props
- Include **default values** where appropriate

```typescript
type CandidateCardProps = {
    candidate: Candidate;
    index: number;
    onClick: (candidate: Candidate) => void;
};

const CandidateCard: React.FC<CandidateCardProps> = ({ candidate, index, onClick }) => {
    // Component implementation
};
```

### State Management

#### Local State with Hooks
- Use **useState** for component-level state
- Use **useEffect** for side effects (not as the default data-fetching mechanism)
- **Extract custom hooks** for reusable stateful logic
- Use **TanStack Query** for server data fetching, retries, and cache invalidation
- Prefer **Server Components and route loaders** when using framework-native data APIs

```typescript
const [formData, setFormData] = useState({
    title: '',
    description: '',
    status: 'Draft'
});

const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
        ...prev,
        [name]: value
    }));
};
```

```typescript
// Preferred pattern for server state
const { data, isLoading, isError, error } = useQuery({
    queryKey: ['positions'],
    queryFn: positionService.getAllPositions,
    staleTime: 60_000
});
```

#### Loading and Error States
- **Always handle loading states** for async operations
- **Implement error handling** with user-friendly messages
- **Use accessible alert patterns** (role="alert") for feedback

```typescript
const [loading, setLoading] = useState(true);
const [error, setError] = useState('');
const [success, setSuccess] = useState('');

// In async function
try {
    setLoading(true);
    const data = await apiCall();
    setSuccess('Operation completed successfully');
} catch (error) {
    setError('Error message: ' + error.message);
} finally {
    setLoading(false);
}
```

### Service Layer Architecture

#### API Services
- **Centralize API calls** in service files
- Use **fetch** as the standard HTTP client across the project
- **Export service objects** with grouped methods
- **Handle errors at service level** when appropriate

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:3010';

export const positionService = {
    getAllPositions: async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/positions`);
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching positions:', error);
            throw error;
        }
    },
    
    updatePosition: async (id, positionData) => {
        try {
            const response = await fetch(`${API_BASE_URL}/positions/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(positionData)
            });
            if (!response.ok) {
                throw new Error(`Request failed with status ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error updating position:', error);
            throw error;
        }
    }
};
```

## UI/UX Standards

### Tailwind and Component Libraries
- Use **Tailwind CSS** as the default styling approach
- Build reusable UI on top of **shadcn/ui** components
- Prefer **headless/accessibility-first** components over custom components from scratch
- Use **design tokens** for spacing, typography, color, and elevation consistency
- Avoid ad-hoc one-off styles when a shared token or utility class exists

```tsx
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
```

### Form Handling
- Use **controlled components** for form inputs
- Prefer **React Hook Form + Zod** for typed form schemas and validation
- Implement **real-time validation** where appropriate
- **Disable submit buttons** during form submission
- **Clear form state** after successful submission

```tsx
const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: { title: '', description: '' }
});

<form onSubmit={form.handleSubmit(onSubmit)}>
    <input {...form.register('title')} aria-label="Title" />
    <button type="submit" disabled={saving}>
        {saving ? 'Saving...' : 'Save'}
    </button>
</form>
```

### Navigation Patterns
- Use **framework-native routing** by default (Next.js App Router)
- **Implement breadcrumbs** with back navigation
- Use **programmatic navigation** with Next.js router APIs (`router.push`) only when needed

```tsx
import { useRouter } from 'next/navigation';

const router = useRouter();

// Navigation examples
<button onClick={() => router.push('/')}>
    ← Back to Dashboard
</button>
```

### Accessibility
- Include **aria-label** attributes for interactive elements
- Use **semantic HTML** elements
- Ensure **keyboard navigation** support
- Provide **alternative text** for images
- Enforce **minimum contrast ratios** and visible focus states
- Validate accessibility with automated checks and manual keyboard/screen reader testing

```tsx
<input
    type="text"
    placeholder="Search by title"
    aria-label="Search positions by title"
/>
```

## Testing Standards

### End-to-End Testing with Playwright
- **Test user workflows** rather than implementation details
- Use **data-testid** attributes for reliable element selection
- **Organize tests by feature** (`candidates.spec.ts`, `positions.spec.ts`)
- **Include API-level assertions** where they improve confidence

```typescript
import { test, expect } from '@playwright/test';

test('should update a position successfully', async ({ request }) => {
    const testPositionId = '123';
    const updateData = {
        title: 'Updated Test Position',
        status: 'Open'
    };

    const response = await request.put(`${process.env.API_URL}/positions/${testPositionId}`, {
        data: updateData
    });

    expect(response.status()).toBe(200);
    const payload = await response.json();
    expect(payload.data.title).toBe(updateData.title);
});
```

### Test Organization
- **Group related tests** with describe blocks
- **Use descriptive test names** that explain the expected behavior
- **Test both success and error scenarios**
- **Include edge cases** and validation testing
- **Use Vitest** for all unit and integration tests

## Configuration Standards

### TypeScript Configuration
- Enable **strict mode** for type checking
- Use **path mapping** with "@/*" for cleaner imports
- Include framework/test types used by the project
- Prefer **modern targets** (`ES2022` or newer) aligned with project browser/runtime support

```json
{
    "compilerOptions": {
        "strict": true,
        "baseUrl": ".",
        "paths": {
            "@/*": ["src/*"]
        },
        "types": ["node", "vitest/globals", "@testing-library/jest-dom"]
    }
}
```

### ESLint Configuration
- Extend **modern React + TypeScript** configuration
- Include **Vitest rules** for testing
- **Automatic code formatting** and error detection
- **Consistent code style** across the project

### Environment Configuration
- Use **environment variables** for API URLs
- **Separate configurations** for development and production
- **Configure Playwright** with environment-specific settings

```typescript
// playwright.config.ts
export default defineConfig({
    use: {
        baseURL: 'http://localhost:3000'
    }
});
```

## Performance Best Practices

### Component Optimization
- **Lazy load** components when appropriate
- **Memoize expensive calculations** with useMemo
- **Avoid unnecessary re-renders** with useCallback
- **Extract reusable logic** into custom hooks
- Prefer **Server Components** to reduce shipped JavaScript when using Next.js

### Bundle Optimization
- **Tree shaking** enabled through Next.js build pipeline
- **Code splitting** at route level
- **Optimize images** and static assets
- **Monitor bundle size** with build tools
- Use **streaming SSR** where applicable to improve TTFB and perceived performance

### API Efficiency
- **Implement proper error handling** for network requests
- **Cache API responses** through TanStack Query and framework caches
- **Use loading states** to improve perceived performance
- **Batch API calls** when possible

## Development Workflow

### Git Workflow
- **Feature Branches**: Develop features in separate branches, adding descriptive suffix "-frontend" to allow working in parallel and avoid conflicts or collisions
- **Descriptive Commits**: Write descriptive commit messages in English
- **Code Review**: Code review before merging
- **Small Branches**: Keep branches small and focused
- **Component-Driven Development**: Build and validate reusable components in Storybook
- **API-First Collaboration**: Align frontend/backend contracts via OpenAPI before implementation
- **Monorepo Readiness**: Standardize on `pnpm` workspaces with Turborepo when sharing types/components across frontend and backend

### Development Scripts
```bash
npm run dev             # Development server
npm run test            # Run unit/integration tests (Vitest)
npm run test:e2e        # Run Playwright tests
npm run build           # Production build
npm run storybook       # Run Storybook locally
```

### Code Quality
- **ESLint validation** before commits
- **TypeScript compilation** without errors
- **All tests passing** before deployment
- **Performance monitoring** with Web Vitals
- **Accessibility checks** as part of the CI quality gate

## Migration Strategy

### TypeScript Migration
- **Complete migration** from JavaScript to TypeScript
- **Require TypeScript** for all new and modified components
- **Remove remaining JavaScript components** as part of scheduled refactors
- **Enforce strict typing** in all feature modules

### Component Modernization
- **Functional components** over class components
- **Hooks** instead of lifecycle methods
- **Tailwind + accessible primitives** for UI consistency
- **Responsive design** principles throughout
- **Use Next.js patterns** (Server Components, SSR, and streaming) as the default implementation model
- **Use Vitest and Playwright** as the only supported testing stack

This document serves as the foundation for maintaining code quality and consistency across the frontend application. All team members should follow these practices to ensure a maintainable and scalable codebase.