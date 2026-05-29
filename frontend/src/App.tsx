import AiInputPreview from './components/AiInputPreview'
import AppShell from './components/AppShell'
import CalendarPreview from './components/CalendarPreview'
import HeroPanel from './components/HeroPanel'
import StatusCard from './components/StatusCard'

const capabilities = [
  {
    title: '自然语言安排日程',
    description: '把日常表达转换成清晰的日程草稿，减少来回确认。',
    accent: 'from-emerald-300 to-cyan-300',
  },
  {
    title: '日历事件管理',
    description: '在一个专注的工作区里查看安排、准备会议和管理事件。',
    accent: 'from-violet-300 to-fuchsia-300',
  },
  {
    title: '后端工具调用就绪',
    description: '已为 LangChain4j 工具调用打好基础，后续可接入真实模型。',
    accent: 'from-amber-200 to-rose-300',
  },
]

function App() {
  return (
    <AppShell>
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-10 px-5 py-6 sm:px-8 lg:px-10">
        <nav className="flex items-center justify-between rounded-full border border-white/10 bg-white/[0.06] px-4 py-3 shadow-2xl shadow-black/20 backdrop-blur-xl">
          <div className="flex items-center gap-3">
            <div className="grid size-10 place-items-center rounded-2xl bg-gradient-to-br from-emerald-300 via-cyan-300 to-violet-300 text-sm font-black text-slate-950 shadow-lg shadow-cyan-500/20">
              VC
            </div>
            <div>
              <p className="text-sm font-semibold text-white">VoiceCal Agent</p>
              <p className="text-xs text-slate-400">AI 日程助手</p>
            </div>
          </div>
          <div className="rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs font-medium text-emerald-100">
            本地演示
          </div>
        </nav>

        <section className="grid items-center gap-8 lg:grid-cols-[1.05fr_0.95fr]">
          <HeroPanel />
          <div className="grid gap-4">
            <AiInputPreview />
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
              <CalendarPreview />
              <StatusCard />
            </div>
          </div>
        </section>

        <section className="grid gap-4 pb-8 md:grid-cols-3">
          {capabilities.map((capability) => (
            <article
              className="group rounded-[28px] border border-white/10 bg-white/[0.06] p-6 shadow-2xl shadow-black/20 backdrop-blur-xl transition duration-300 hover:-translate-y-1 hover:border-white/20 hover:bg-white/[0.09]"
              key={capability.title}
            >
              <div
                className={`mb-6 h-1.5 w-16 rounded-full bg-gradient-to-r ${capability.accent}`}
              />
              <h2 className="text-lg font-semibold text-white">{capability.title}</h2>
              <p className="mt-3 text-sm leading-6 text-slate-400">{capability.description}</p>
            </article>
          ))}
        </section>
      </main>
    </AppShell>
  )
}

export default App
