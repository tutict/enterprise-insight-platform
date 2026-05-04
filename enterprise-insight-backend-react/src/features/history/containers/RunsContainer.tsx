import { useRunsPage } from '../hooks/useRunsPage'
import RunsWorkspace from '../components/RunsWorkspace'

export default function RunsContainer() {
  const runsPage = useRunsPage()

  return <RunsWorkspace {...runsPage} />
}
