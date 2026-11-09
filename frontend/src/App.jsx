import { useCallback, useEffect, useState } from 'react'
import './App.css'

const API_URL = '/api/practices'

function today() {
  const date = new Date()
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
  return localDate.toISOString().slice(0, 10)
}

function emptyForm() {
  return {
    practiceDate: today(),
    content: '',
    durationMin: '',
    reflection: '',
    nextGoal: '',
  }
}

function formatDate(date) {
  return new Intl.DateTimeFormat('ja-JP', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date(`${date}T00:00:00`))
}

function formatDateTime(dateTime) {
  if (!dateTime) return '—'
  // BackendのLocalDateTimeはタイムゾーン情報を持たないため、日本時間として解釈する
  const japanDateTime = new Date(`${dateTime}+09:00`)
  return new Intl.DateTimeFormat('ja-JP', {
    timeZone: 'Asia/Tokyo',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).format(japanDateTime)
}

async function readError(response) {
  try {
    const body = await response.json()
    const messages = body.errors ? Object.values(body.errors) : []
    return messages.length > 0 ? messages.join(' / ') : body.message || `処理に失敗しました。（${response.status}）`
  } catch {
    return `サーバーからエラー応答がありました。（${response.status}）`
  }
}

function connectionError() {
  return 'バックエンドに接続できません。Docker Desktopを起動し、backendフォルダーで docker compose up -d を実行してください。'
}

function App() {
  const [user, setUser] = useState(null)
  const [authLoading, setAuthLoading] = useState(true)
  const [authMode, setAuthMode] = useState('login')
  const [authForm, setAuthForm] = useState({ loginId: '', password: '', userName: '', role: 'EDITOR' })
  const [authError, setAuthError] = useState('')
  const [csrf, setCsrf] = useState({ token: '', headerName: 'X-CSRF-TOKEN' })
  const [records, setRecords] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [selectedRecord, setSelectedRecord] = useState(null)
  const [view, setView] = useState('list')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const canEdit = user?.role === 'EDITOR'

  const refreshCsrfToken = useCallback(async () => {
    const response = await fetch('/api/auth/csrf')
    if (!response.ok) throw new Error(await readError(response))
    const token = await response.json()
    setCsrf(token)
    return token
  }, [])

  useEffect(() => {
    async function restoreLogin() {
      try {
        await refreshCsrfToken()
        const response = await fetch('/api/auth/me')
        if (response.ok) {
          setUser(await response.json())
        } else if (response.status !== 401) {
          setAuthError(await readError(response))
        }
      } catch (err) {
        setAuthError(err instanceof TypeError ? connectionError() : err.message)
      } finally {
        setAuthLoading(false)
      }
    }
    restoreLogin()
  }, [refreshCsrfToken])

  const loadRecords = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(API_URL)
      if (response.status === 401) {
        setUser(null)
        setAuthError('')
        setAuthMode('login')
        return
      }
      if (!response.ok) throw new Error(await readError(response))
      setRecords(await response.json())
    } catch (err) {
      setError(err instanceof TypeError ? connectionError() : err.message || '記録を読み込めませんでした。')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (user) loadRecords()
  }, [loadRecords, user])

  function updateAuthField(event) {
    const { name, value } = event.target
    setAuthForm((current) => ({ ...current, [name]: value }))
  }

  function chooseAuthMode(mode) {
    setAuthMode(mode)
    setAuthError('')
  }

  async function submitAuth(event) {
    event.preventDefault()
    setAuthError('')
    try {
      const token = await refreshCsrfToken()
      const headers = { 'Content-Type': 'application/json', [token.headerName]: token.token }

      if (authMode === 'register') {
        const registerResponse = await fetch('/api/auth/register', {
          method: 'POST',
          headers,
          body: JSON.stringify(authForm),
        })
        if (!registerResponse.ok) throw new Error(await readError(registerResponse))
      }

      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers,
        body: JSON.stringify({ loginId: authForm.loginId, password: authForm.password }),
      })
      if (!response.ok) throw new Error(await readError(response))

      setUser(await response.json())
      setAuthForm((current) => ({ ...current, password: '' }))
      await refreshCsrfToken()
      setAuthError('')
    } catch (err) {
      setAuthError(err instanceof TypeError ? connectionError() : err.message || '認証できませんでした。')
    }
  }

  async function logout() {
    setAuthError('')
    try {
      const token = await refreshCsrfToken()
      const response = await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { [token.headerName]: token.token },
      })
      if (!response.ok) throw new Error(await readError(response))
      setUser(null)
      setAuthMode('login')
      setAuthError('')
      setView('list')
      setRecords([])
      await refreshCsrfToken()
    } catch (err) {
      setError(err instanceof TypeError ? connectionError() : err.message || 'ログアウトできませんでした。')
    }
  }

  function updateField(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function openNewForm() {
    if (!canEdit) return
    setEditingId(null)
    setForm(emptyForm())
    setError('')
    setNotice('')
    setView('form')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function startEditing(record) {
    if (!canEdit) return
    setSelectedRecord(null)
    setEditingId(record.practiceId)
    setForm({
      practiceDate: record.practiceDate,
      content: record.content,
      durationMin: String(record.durationMin),
      reflection: record.reflection || '',
      nextGoal: record.nextGoal || '',
    })
    setError('')
    setNotice('')
    setView('form')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function openDetails(record, index) {
    setSelectedRecord({ ...record, listNumber: index + 1 })
    setError('')
    setNotice('')
    setView('detail')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function returnToList() {
    setEditingId(null)
    setSelectedRecord(null)
    setForm(emptyForm())
    setError('')
    setNotice('')
    setView('list')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function submitForm(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setNotice('')

    const isEditing = editingId !== null
    const payload = {
      ...form,
      durationMin: Number(form.durationMin),
      reflection: form.reflection.trim() || null,
      nextGoal: form.nextGoal.trim() || null,
    }

    try {
      const response = await fetch(isEditing ? `${API_URL}/${editingId}` : API_URL, {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
        body: JSON.stringify(payload),
      })
      if (!response.ok) throw new Error(await readError(response))

      setEditingId(null)
      setForm(emptyForm())
      setView('list')
      setNotice(isEditing ? '練習記録を更新しました。' : '練習記録を保存しました。')
      window.scrollTo({ top: 0, behavior: 'smooth' })
      await loadRecords()
    } catch (err) {
      setError(err instanceof TypeError ? connectionError() : err.message || '保存できませんでした。')
    } finally {
      setSaving(false)
    }
  }

  async function deleteRecord(record) {
    if (!window.confirm(`「${record.content}」の記録を削除しますか？`)) return

    setError('')
    setNotice('')
    try {
      const response = await fetch(`${API_URL}/${record.practiceId}`, {
        method: 'DELETE',
        headers: { [csrf.headerName]: csrf.token },
      })
      if (!response.ok) throw new Error(await readError(response))
      setNotice('練習記録を削除しました。')
      await loadRecords()
    } catch (err) {
      setError(err instanceof TypeError ? connectionError() : err.message || '削除できませんでした。')
    }
  }

  if (authLoading) {
    return (
      <main className="app-shell">
        <header className="topbar">
          <span className="brand"><span className="brand-mark">S</span><span>SOFT TENNIS <strong>NOTE</strong></span></span>
        </header>
        <div className="auth-loading">ログイン状態を確認しています…</div>
      </main>
    )
  }

  if (!user) {
    const registering = authMode === 'register'
    return (
      <main className="app-shell">
        <header className="topbar">
          <span className="brand"><span className="brand-mark">S</span><span>SOFT TENNIS <strong>NOTE</strong></span></span>
          <span className="topbar-caption">練習を、次の成長につなげる。</span>
        </header>
        <section className="auth-layout">
          <div className="auth-card">
            <p className="eyebrow">ACCOUNT</p>
            <h1>{registering ? 'アカウント作成' : 'ログイン'}</h1>
            <p className="auth-description">{registering ? '利用区分を選んでアカウントを作成してください。' : 'ログインして練習記録を確認しましょう。'}</p>
            {authError && <p className="error-banner" role="alert">{authError}</p>}
            <form className="auth-form" onSubmit={submitAuth}>
              {registering && <>
                <label className="field-group"><span className="field-label">表示名</span><input name="userName" autoComplete="name" maxLength={80} value={authForm.userName} onChange={updateAuthField} required /></label>
                <label className="field-group"><span className="field-label">利用区分</span><select name="role" value={authForm.role} onChange={updateAuthField}><option value="EDITOR">選手・指導者（登録・編集可）</option><option value="VIEWER">保護者・関係者（閲覧のみ）</option></select></label>
              </>}
              <label className="field-group"><span className="field-label">ログインID</span><input name="loginId" autoComplete="username" minLength={3} maxLength={100} value={authForm.loginId} onChange={updateAuthField} required /></label>
              <label className="field-group"><span className="field-label">パスワード</span><input name="password" type="password" autoComplete={registering ? 'new-password' : 'current-password'} minLength={8} maxLength={64} value={authForm.password} onChange={updateAuthField} required />{registering && <span className="auth-hint">8文字以上で入力してください。</span>}</label>
              <button className="primary-button auth-submit" type="submit">{registering ? '作成してログイン' : 'ログイン'} <span aria-hidden="true">→</span></button>
            </form>
            <button className="auth-switch" type="button" onClick={() => chooseAuthMode(registering ? 'login' : 'register')}>
              {registering ? 'すでにアカウントをお持ちの方はこちら' : '初めて使う方はアカウントを作成'}
            </button>
          </div>
        </section>
        <footer className="footer">SOFT TENNIS NOTE <span>·</span> 一球ずつ、前へ。</footer>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <button className="brand" type="button" onClick={returnToList} aria-label="練習一覧へ戻る">
          <span className="brand-mark" aria-hidden="true">S</span>
          <span>SOFT TENNIS <strong>NOTE</strong></span>
        </button>
        <div className="topbar-actions">
          {user ? <>
            <span className="topbar-caption">{user.userName} さん・{canEdit ? '編集可能' : '閲覧のみ'}</span>
            <button className="logout-button" type="button" onClick={logout}>ログアウト</button>
          </> : <>
            <button className="logout-button" type="button" onClick={() => chooseAuthMode('login')}>ログイン</button>
            <button className="logout-button" type="button" onClick={() => chooseAuthMode('register')}>アカウント作成</button>
          </>}
        </div>
      </header>

      {error && view !== 'list' && <p className="error-banner" role="alert">{error}</p>}

      {view === 'list' ? (
        <>
          <section className="hero" aria-label="練習ノートの紹介">
            <div className="hero-copy">
              <p className="eyebrow">YOUR PRACTICE JOURNAL</p>
              <h1>練習の積み重ねを、<br /><span>自信に変えよう。</span></h1>
              <p className="hero-description">今日の気づきを記録して、次の一歩へ。</p>
            </div>
            <div className="hero-stat" aria-label={`${records.length}件の練習記録`}>
              <span className="stat-number">{records.length}</span>
              <span className="stat-label">TOTAL PRACTICES</span>
              <span className="stat-caption">これまでの記録</span>
            </div>
            <svg className="court-lines" viewBox="0 0 520 260" aria-hidden="true">
              <g fill="none" stroke="currentColor" strokeWidth="1.5">
                <rect x="8" y="8" width="504" height="244" />
                <line x1="8" y1="38" x2="512" y2="38" />
                <line x1="8" y1="222" x2="512" y2="222" />
                <line x1="126" y1="38" x2="126" y2="222" />
                <line x1="394" y1="38" x2="394" y2="222" />
                <line x1="260" y1="8" x2="260" y2="252" />
                <line x1="126" y1="130" x2="394" y2="130" />
              </g>
            </svg>
          </section>

          <section className="page-heading">
            <div>
              <p className="eyebrow">YOUR HISTORY</p>
              <h2>練習一覧</h2>
              <p className="page-description">日々の練習を振り返り、次の目標につなげましょう。</p>
            </div>
            {canEdit && <button className="primary-button mobile-register" type="button" onClick={openNewForm}>
              <span aria-hidden="true">＋</span> 登録
            </button>}
          </section>

          {notice && <p className="notice" role="status">{notice}</p>}
          {error && <p className="error-banner" role="alert">{error}</p>}

          <div className="list-layout">
            <section className="records-section" aria-labelledby="records-title">
              <div className="section-heading">
                <div>
                  <h2 id="records-title">練習記録の一覧</h2>
                  <p>{records.length}件の記録</p>
                </div>
                <button className="refresh-button" type="button" onClick={loadRecords} disabled={loading}>
                  <span aria-hidden="true">↻</span> 更新
                </button>
              </div>

              {loading ? (
                <div className="empty-state"><span className="loader" />記録を読み込んでいます…</div>
              ) : records.length === 0 ? (
                <div className="empty-state">
                  <span className="empty-icon" aria-hidden="true">✦</span>
                  <strong>まだ記録がありません</strong>
                  <span>{canEdit ? '「練習を登録」から最初の記録を作りましょう。' : 'まだ練習記録がありません。'}</span>
                </div>
              ) : (
                <div className="record-list">
                  {records.map((record, index) => (
                    <article className="record-card" key={record.practiceId}>
                      <button
                        className="record-open"
                        type="button"
                        onClick={() => openDetails(record, index)}
                        aria-label={`${String(index + 1).padStart(2, '0')}番、${formatDate(record.practiceDate)}、${record.durationMin}分、${record.content}。詳細を見る`}
                      >
                        <span className="record-marker" aria-hidden="true">{String(index + 1).padStart(2, '0')}</span>
                        <span className="record-main">
                          <span className="record-meta">
                            <time dateTime={record.practiceDate}>{formatDate(record.practiceDate)}</time>
                            <span className="duration">◷ {record.durationMin}分</span>
                          </span>
                          <span className="record-title">{record.content}</span>
                        </span>
                      </button>
                      {canEdit && <div className="record-actions">
                        <button type="button" onClick={() => startEditing(record)} aria-label={`${record.content}を編集`}>編集</button>
                        <button className="delete-action" type="button" onClick={() => deleteRecord(record)} aria-label={`${record.content}を削除`}>削除</button>
                      </div>}
                    </article>
                  ))}
                </div>
              )}
            </section>

            <aside id="account-panel" className={`list-aside ${!user ? 'auth-visible' : ''}`}>
              {!user ? (
                <>
                  <p className="eyebrow">ACCOUNT</p>
                  {authMode === 'landing' ? (
                    <>
                      <button className="primary-button auth-landing-login" type="button" onClick={() => chooseAuthMode('login')}>ログイン <span aria-hidden="true">→</span></button>
                      <button className="auth-switch auth-landing-register" type="button" onClick={() => chooseAuthMode('register')}>初めて使う方はアカウントを作成</button>
                      <p className="aside-tip"><span aria-hidden="true">✦</span><span>未ログインでも一覧と詳細を確認できます。</span></p>
                    </>
                  ) : (
                    <>
                      <h2 className="auth-aside-title">{authMode === 'register' ? 'アカウント作成' : 'ログイン'}</h2>
                      <p className="auth-description">{authMode === 'register' ? '利用区分を選んでください。' : 'ログインIDとパスワードを入力してください。'}</p>
                      {authError && <p className="error-banner" role="alert">{authError}</p>}
                      <form className="auth-form" onSubmit={submitAuth}>
                        {authMode === 'register' && <>
                          <label className="field-group"><span className="field-label">表示名</span><input name="userName" autoComplete="name" maxLength={80} value={authForm.userName} onChange={updateAuthField} required /></label>
                          <label className="field-group"><span className="field-label">利用区分</span><select name="role" value={authForm.role} onChange={updateAuthField}><option value="EDITOR">選手・指導者（登録・編集可）</option><option value="VIEWER">保護者・関係者（閲覧のみ）</option></select></label>
                        </>}
                        <label className="field-group"><span className="field-label">ログインID</span><input name="loginId" autoComplete="username" minLength={3} maxLength={100} value={authForm.loginId} onChange={updateAuthField} required /></label>
                        <label className="field-group"><span className="field-label">パスワード</span><input name="password" type="password" autoComplete={authMode === 'register' ? 'new-password' : 'current-password'} minLength={8} maxLength={64} value={authForm.password} onChange={updateAuthField} required />{authMode === 'register' && <span className="auth-hint">8文字以上で入力してください。</span>}</label>
                        <button className="primary-button auth-submit" type="submit">{authMode === 'register' ? '作成してログイン' : 'ログイン'} <span aria-hidden="true">→</span></button>
                      </form>
                      <button className="auth-switch" type="button" onClick={() => { setAuthMode(authMode === 'register' ? 'login' : 'register'); setAuthError('') }}>{authMode === 'register' ? 'すでにアカウントをお持ちの方はこちら' : '初めて使う方はアカウントを作成'}</button>
                      <button className="auth-back" type="button" onClick={() => { setAuthMode('landing'); setAuthError('') }}>一覧へ戻る</button>
                    </>
                  )}
                </>
              ) : <>
              <p className="eyebrow">YOUR JOURNAL</p>
              <div className="list-count"><strong>{records.length}</strong><span>件</span></div>
              <p className="aside-caption">これまでの練習記録</p>
              {canEdit ? <button className="primary-button aside-register" type="button" onClick={openNewForm}>練習を登録 <span aria-hidden="true">→</span></button> : <p className="aside-caption">閲覧専用アカウントです。</p>}
              <div className="aside-tip">
                <span aria-hidden="true">✦</span>
                <p>小さな気づきも記録して、次の練習に活かしましょう。</p>
              </div>
              </>}
            </aside>
          </div>
        </>
      ) : view === 'detail' && selectedRecord ? (
        <>
          <button className="back-button" type="button" onClick={returnToList}>
            <span aria-hidden="true">←</span> 練習一覧に戻る
          </button>
          <section className="form-page-heading">
            <p className="eyebrow">PRACTICE DETAILS</p>
            <h1>練習記録の詳細</h1>
            <p>登録した内容を確認できます。</p>
          </section>

          <section className="detail-card" aria-labelledby="detail-title">
            <div className="detail-card-heading">
              <span className="record-marker">{String(selectedRecord.listNumber).padStart(2, '0')}</span>
              <div>
                <p className="eyebrow">PRACTICE RECORD</p>
                <h2 id="detail-title">{selectedRecord.content}</h2>
              </div>
              <span className="duration detail-duration">◷ {selectedRecord.durationMin}分</span>
            </div>

            <dl className="detail-list">
              <div className="detail-item">
                <dt>練習日</dt>
                <dd>{formatDate(selectedRecord.practiceDate)}</dd>
              </div>
              <div className="detail-item">
                <dt>練習時間</dt>
                <dd>{selectedRecord.durationMin}分</dd>
              </div>
              <div className="detail-item detail-item-wide">
                <dt>練習内容</dt>
                <dd>{selectedRecord.content}</dd>
              </div>
              <div className="detail-item detail-item-wide">
                <dt>振り返り</dt>
                <dd>{selectedRecord.reflection || '未入力'}</dd>
              </div>
              <div className="detail-item detail-item-wide">
                <dt>次回の目標</dt>
                <dd>{selectedRecord.nextGoal || '未入力'}</dd>
              </div>
              <div className="detail-item detail-timestamp">
                <dt>登録日時</dt>
                <dd>{formatDateTime(selectedRecord.createdAt)}</dd>
              </div>
              <div className="detail-item detail-timestamp">
                <dt>最終更新日時</dt>
                <dd>{formatDateTime(selectedRecord.updatedAt)}</dd>
              </div>
            </dl>

            <div className="detail-actions">
              {canEdit && <button className="primary-button" type="button" onClick={() => startEditing(selectedRecord)}>この記録を編集</button>}
              <button className="secondary-button" type="button" onClick={returnToList}>一覧へ戻る</button>
            </div>
          </section>
        </>
      ) : canEdit ? (
        <>
          <button className="back-button" type="button" onClick={returnToList}>
            <span aria-hidden="true">←</span> 練習一覧に戻る
          </button>
          <section className="form-page-heading">
            <p className="eyebrow">MAKE IT COUNT</p>
            <h1>{editingId ? '練習記録の編集' : '練習登録'}</h1>
            <p>今日の練習を記録して、次の一歩につなげましょう。</p>
          </section>
          {error && <p className="error-banner form-error" role="alert">{error}</p>}

          <section className="form-panel" aria-labelledby="form-title">
            <div className="form-heading">
              <div className="form-icon" aria-hidden="true">✦</div>
              <div>
                <h2 id="form-title">{editingId ? '記録内容を編集' : '練習内容を入力'}</h2>
                <p>あとから振り返れるよう、気づいたことも残しておきましょう。</p>
              </div>
            </div>

            <form onSubmit={submitForm}>
              <div className="form-fields">
                <div className="field-group">
                  <label className="field-label" htmlFor="practiceDate">練習日 <span>必須</span></label>
                  <input id="practiceDate" name="practiceDate" type="date" value={form.practiceDate} onChange={updateField} required />
                </div>

                <div className="field-group">
                  <label className="field-label" htmlFor="content">練習内容 <span>必須</span></label>
                  <textarea id="content" name="content" value={form.content} onChange={updateField} maxLength={200} rows={3} placeholder="例：サーブ練習、ストローク" required />
                  <div className="character-count">{form.content.length} / 200</div>
                </div>

                <div className="field-group field-duration">
                  <label className="field-label" htmlFor="durationMin">練習時間 <span>必須</span></label>
                  <div className="input-with-unit">
                    <input id="durationMin" name="durationMin" type="number" min="1" max="999" value={form.durationMin} onChange={updateField} placeholder="90" required />
                    <span>分</span>
                  </div>
                </div>

                <div className="field-group">
                  <label className="field-label" htmlFor="reflection">振り返り <span className="optional">任意</span></label>
                  <textarea id="reflection" name="reflection" value={form.reflection} onChange={updateField} maxLength={500} rows={4} placeholder="うまくいったこと、次に改善したいこと" />
                </div>

                <div className="field-group">
                  <label className="field-label" htmlFor="nextGoal">次回の目標 <span className="optional">任意</span></label>
                  <input id="nextGoal" name="nextGoal" type="text" value={form.nextGoal} onChange={updateField} maxLength={200} placeholder="例：トスを安定させる" />
                </div>
              </div>

              <div className="form-actions">
                <button className="primary-button submit-button" type="submit" disabled={saving}>
                  {saving ? '保存中…' : editingId ? '変更を保存' : '記録を保存'}
                  <span aria-hidden="true">→</span>
                </button>
                <button className="secondary-button" type="button" onClick={returnToList} disabled={saving}>キャンセル</button>
              </div>
            </form>
          </section>
        </>
      ) : <p className="error-banner" role="alert">閲覧専用アカウントでは登録・編集できません。</p>}

      <footer className="footer">SOFT TENNIS NOTE <span>·</span> 一球ずつ、前へ。</footer>
    </main>
  )
}

export default App
